package ohnosequences.sbt

import sbt._, sbt.Keys._
import java.lang.System.getProperty

import org.kohsuke.github._
import scala.collection.JavaConverters._
import scala.util.Try

case object GithubRelease {

  type DefTask[X] = Def.Initialize[Task[X]]
  type DefSetting[X] = Def.Initialize[Setting[X]]

  val ghreleaseDefaultTokenEnvVar: String = "GITHUB_TOKEN"
  val ghreleaseDefaultTokenFile: File = file(s"${getProperty("user.home")}/.github")

  case object keys {
    type TagName = String

    lazy val ghreleaseRepoOrg       = settingKey[String]("Github repository organization")
    lazy val ghreleaseRepoName      = settingKey[String]("Github repository name")
    lazy val ghreleaseMediaTypesMap = settingKey[File => String]("This function will determine media type for the assets")
    lazy val ghreleaseNotes         = settingKey[TagName => String]("Release notes for the given tag")
    lazy val ghreleaseTitle         = settingKey[TagName => String]("The title of the release")
    lazy val ghreleaseIsPrerelease  = settingKey[TagName => Boolean]("A function to determine release as a prerelease based on the tag name")
    lazy val ghreleaseGithubToken   = settingKey[Option[String]]("Credentials for accessing the GitHub API")
    lazy val ghreleaseAssets        = taskKey[Seq[File]]("The artifact files to upload")

    // TODO: remove this, make them tasks or parameters for the main task
    // lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")
    
    lazy val ghreleaseGetRepo = taskKey[GHRepository]("Checks repo existence and returns it if it's fine")

    lazy val ghreleaseGetReleaseBuilder = inputKey[GHReleaseBuilder]("Checks remote tag and returns empty release builder if everything is fine")

    lazy val githubRelease = inputKey[GHRelease]("Publishes a release of Github")
  }

  case object defs {
    import keys._

    ghreleaseGithubToken := {
      ghreleaseGithubTokenFromEnv(ghreleaseDefaultTokenEnvVar) orElse
      ghreleaseGithubTokenFromFile(ghreleaseDefaultTokenFile)
    }
    
    def ghreleaseMediaTypesMap: File => String = {
      val typeMap = new javax.activation.MimetypesFileTypeMap()
      // NOTE: github doesn't know about application/java-archive type (see https://developer.github.com/v3/repos/releases/#input-2)
      typeMap.addMimeTypes("application/zip jar zip")
      // and .pom is unlikely to be in the system's default MIME types map
      typeMap.addMimeTypes("application/xml pom xml")

      typeMap.getContentType
    }

    val ghreleaseDefaultPropertiesFile: sbt.File = file(s"${getProperty("user.home")}/.github")

    def ghreleaseGithubTokenFromEnv(environmentVariableName: String): Option[String] = {
      System.getenv().asScala.get(environmentVariableName)
    }
    
    def ghreleaseGithubTokenFromFile(file: File): Option[String] = {
      val credentialsFile = Option(file)
        .filter(_.isFile)
        .filter(_.canRead)

      val maybeCredentialParameters = credentialsFile.map { githubCredentialsFile =>
        val props = new java.util.Properties()
        props.load(new java.io.FileInputStream(githubCredentialsFile))
        props.asScala
      }

      maybeCredentialParameters.flatMap(_.get("oauth"))
    }

    def ghreleaseGetRepo: DefTask[GHRepository] = Def.task {
      val gitHubCredentials = ghreleaseGithubToken.value.getOrElse(
        sys.error(s"Please provide github credentials in you build by setting the `ghreleaseGithubToken` key!")
      )

      val github = GitHub.connectUsingOAuth(gitHubCredentials)
      if (!github.isCredentialValid) {
        sys.error("No GitHub credentials found !")
      }

      val repo = s"${ghreleaseRepoOrg.value}/${ghreleaseRepoName.value}"

      Try {
        github.getRepository(repo)
      } getOrElse {
        sys.error(s"Repository ${repo} doesn't exist or is not accessible.")
      }
    }

    def ghreleaseGetReleaseBuilder(tagName: String): DefTask[GHReleaseBuilder] = Def.task {
      val repo = ghreleaseGetRepo.value

      val tagNames = repo.listTags.asScala.map(_.getName).toSet
      if (! tagNames.contains(tagName)) {
        sys.error(s"Remote repository doesn't have [${tagName}] tag. You need to push it first.")
      }

      def releaseExists: Boolean =
        repo.listReleases.asScala.map(_.getTagName).toSet.contains(tagName)

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
