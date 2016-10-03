
```scala
package ohnosequences.sbt

import sbt._, Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._
import scala.util.Try

case object GithubRelease {

  type DefTask[X] = Def.Initialize[Task[X]]
  type DefSetting[X] = Def.Initialize[Setting[X]]

  case object keys {
    type TagName = String

    lazy val ghreleaseRepoOrg       = settingKey[String]("Github repository organization")
    lazy val ghreleaseRepoName      = settingKey[String]("Github repository name")
    lazy val ghreleaseMediaTypesMap = settingKey[File => String]("This function will determine media type for the assets")
    lazy val ghreleaseNotes         = settingKey[TagName => String]("Release notes for the given tag")
    lazy val ghreleaseTitle         = settingKey[TagName => String]("The title of the release")
    lazy val ghreleaseIsPrerelease  = settingKey[TagName => Boolean]("A function to determine release as a prerelease based on the tag name")

    lazy val ghreleaseAssets        = taskKey[Seq[File]]("The artifact files to upload")

    // TODO: remove this, make them tasks or parameters for the main task
    // lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")

    lazy val ghreleaseGetCredentials = taskKey[GitHub]("Checks authentification and suggests to create a new oauth token if needed")
    lazy val ghreleaseGetRepo        = taskKey[GHRepository]("Checks repo existence and returns it if it's fine")

    lazy val ghreleaseGetReleaseBuilder = inputKey[GHReleaseBuilder]("Checks remote tag and returns empty release builder if everything is fine")

    lazy val githubRelease = inputKey[GHRelease]("Publishes a release of Github")
  }

  case object defs {
    import keys._

    def ghreleaseMediaTypesMap: File => String = {
      val typeMap = new javax.activation.MimetypesFileTypeMap()
      // NOTE: github doesn't know about application/java-archive type (see https://developer.github.com/v3/repos/releases/#input-2)
      typeMap.addMimeTypes("application/zip jar zip")
      // and .pom is unlikely to be in the system's default MIME types map
      typeMap.addMimeTypes("application/xml pom xml")

      typeMap.getContentType
    }

    def ghreleaseGetCredentials: DefTask[GitHub] = Def.task {
      val log = streams.value.log
      val conf = file(System.getProperty("user.home")) / ".github"

      while (!conf.exists || !GitHub.connect.isCredentialValid) {
        log.warn("Your github credentials for sbt-github-release plugin are not set yet")
        SimpleReader.readLine("Go to https://github.com/settings/applications \ncreate a new token and paste it here: ") match {
          case Some(token) => {
            try {
              val gh = GitHub.connectUsingOAuth(token)
              if (gh.isCredentialValid) {
                IO.writeLines(conf, Seq("oauth = " + token))//, append = true)
                log.info("Wrote OAuth token to " + conf)
              }
            } catch {
              case e: Exception => log.error(e.toString)
            }
          }
          case _ => sys.error("If you want to use sbt-github-release plugin, you should set credentials correctly")
        }
      }
      log.info("Github credentials are ok")
      GitHub.connect
    }

    def ghreleaseGetRepo: DefTask[GHRepository] = Def.task {
      val log = streams.value.log
      val github = ghreleaseGetCredentials.value
      val repo = s"${ghreleaseRepoOrg.value}/${ghreleaseRepoName.value}"

      val repository = Try { github.getRepository(repo) } getOrElse {
        sys.error(s"Repository ${repo} doesn't exist or is not accessible.")
      }
      repository
    }

    def ghreleaseGetReleaseBuilder(tagName: String): DefTask[GHReleaseBuilder] = Def.task {
      val log = streams.value.log
      val repo = ghreleaseGetRepo.value

      val tagNames = repo.listTags.asSet.map(_.getName)
      if (! tagNames.contains(tagName)) {
        sys.error(s"Remote repository doesn't have [${tagName}] tag. You need to push it first.")
      }

      def releaseExists: Boolean =
        repo.listReleases.asSet.map(_.getTagName).contains(tagName)

      // if (!draft.value && releaseExists) {
      if (releaseExists) {
        sys.error(s"There is already a Github release based on [${tagName}] tag. You cannot release it twice.")
        // TODO: ask to overwrite (+ report if it is a draft)
      }

      repo.createRelease(tagName)
    }

    def githubRelease(tagName: String): DefTask[GHRelease] = Def.taskDyn {
      val log = streams.value.log

      val notes = ghreleaseNotes.value(tagName)
      if (notes.isEmpty) {
        log.warn(s"Release notes are empty")
        SimpleReader.readLine("Are you sure you want to continue without release notes (y/n)? [n] ") match {
          case Some("n" | "N") => sys.error("Aborting release due to empty release notes")
          case _ => // go on
        }
      }

      val isPre = ghreleaseIsPrerelease.value(tagName)

      Def.task {
        val releaseBuilder = ghreleaseGetReleaseBuilder(tagName).value
          .body(notes)
          .name(ghreleaseTitle.value(tagName))
          .prerelease(isPre)

        val release = Try { releaseBuilder.create } getOrElse {
          sys.error("Couldn't create release")
        }

        val pre = if (isPre) "pre-" else ""
        // val pub = if(draft.value) "saved as a draft" else "published"
        log.info(s"Github ${pre}release '${release.getName}' is published at\n  ${release.getHtmlUrl}")

        ghreleaseAssets.value foreach { asset =>
          val mediaType = keys.ghreleaseMediaTypesMap.value(asset)
          val rel = asset.relativeTo(baseDirectory.value).getOrElse(asset)

          release.uploadAsset(asset, mediaType)
          log.info(s"Asset [${rel}] is uploaded to Github as ${mediaType}")
        }

        release
      }
    }
  }
}

```




[main/scala/GithubRelease.scala]: GithubRelease.scala.md
[main/scala/SbtGithubReleasePlugin.scala]: SbtGithubReleasePlugin.scala.md