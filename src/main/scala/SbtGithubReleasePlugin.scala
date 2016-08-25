package ohnosequences.sbt

import sbt._, Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._
import scala.util.Try

case object GithubRelease {

  case object keys {
    // this object is just as a namespace:
    case object GithubRelease {
      lazy val notesDir = settingKey[File]("Directory with release notes")
      lazy val notesFile = settingKey[File]("File with the release notes for the current version")
      lazy val repo = settingKey[String]("org/repo")
      lazy val tag = settingKey[String]("The name of the tag: vX.Y.Z")
      lazy val releaseName = settingKey[String]("The name of the release")
      lazy val commitish = settingKey[String]("Specifies the commitish value that determines where the Git tag is created from")
      lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")
      lazy val prerelease = settingKey[Boolean]("true to identify the release as a prerelease. false to identify the release as a full release")
      lazy val mediaTypesMap = settingKey[File => String]("This function will determine media type for the assets")
      lazy val releaseAssets = taskKey[Seq[File]]("The artifact files to upload")
    }

    lazy val checkGithubCredentials = taskKey[GitHub]("Checks authentification and suggests to create a new oauth token if needed")
    lazy val ghreleaseGetRepo = taskKey[GHRepository]("Checks repo existence and returns it if it's fine")
    lazy val ghreleaseGetReleaseBuilder = taskKey[GHReleaseBuilder]("Checks remote tag and returns empty release builder if everything is fine")

    lazy val releaseOnGithub = taskKey[GHRelease]("Publishes a release of Github")
  }

  case object defs {
    import keys._, keys.GithubRelease._

    def getReleaseBuilder(tagName: String) = Def.task {
      val log = streams.value.log
      val repo = ghreleaseGetRepo.value

      val tagNames = repo.listTags.asSet.map(_.getName)
      if (! tagNames.contains(tagName)) {
        sys.error("Remote repository doesn't have [${tagName}] tag. You need to push it first.")
      }

      def releaseExists: Boolean =
        repo.listReleases.asSet.map(_.getTagName).contains(tagName)

      if (!draft.value && releaseExists) {
        sys.error("There is already a Github release based on [${tagName}] tag. You cannot release it twice.")
      }

      repo.createRelease(tagName)
    }

    def releaseOnGithub = Def.taskDyn {
      if (tag.value.endsWith("-SNAPSHOT"))
        sys.error(s"Current version is '${version.value}'. You shouldn't publish snapshots, maybe you forgot to set the release version")

      val log = streams.value.log

      val text = IO.read(notesFile.value)
      val notesPath = notesFile.value.relativeTo(baseDirectory.value).getOrElse(notesFile.value)
      if (text.isEmpty) {
        log.error(s"Release notes file [${notesPath}] is empty")
        SimpleReader.readLine("Are you sure you want to continue without release notes (y/n)? [n] ") match {
          case Some("n" | "N") => sys.error("Aborting release. Write release notes and try again")
          case _ => // go on
        }
      } else log.info(s"Using release notes from the [${notesPath}] file")

      val tagName = tag.value
      Def.task {
        val releaseBuilder = {
          val rBuilder = getReleaseBuilder(tagName).value
            .body(text)
            .name(releaseName.value)
            .draft(draft.value)
            .prerelease(prerelease.value)

          if (commitish.value.isEmpty) rBuilder
          else rBuilder.commitish(commitish.value)
        }

        val release = Try { releaseBuilder.create } getOrElse {
          sys.error("Couldn't create release")
        }

        val pre = if (prerelease.value) "pre-" else ""
        val pub = if(draft.value) "saved as a draft" else "published"
        log.info(s"Github ${pre}release '${release.getName}' is ${pub} at\n  ${release.getHtmlUrl}")

        releaseAssets.value foreach { asset =>
          val mediaType = mediaTypesMap.value(asset)
          val rel = asset.relativeTo(baseDirectory.value).getOrElse(asset)

          release.uploadAsset(asset, mediaType)
          log.info(s"Asset [${rel}] is uploaded to Github as ${mediaType}")
        }

        release
      }
    }
  }
}

object SbtGithubReleasePlugin extends AutoPlugin {

  // This plugin will load automatically
  override def trigger = allRequirements

  val autoImport = GithubRelease.keys

  import GithubRelease._, keys._, keys.GithubRelease._

  // Default settings
  override lazy val projectSettings = Seq[Setting[_]](
    notesDir := baseDirectory.value / "notes",
    notesFile := notesDir.value / (version.value+".markdown"),
    repo := organization.value +"/"+ normalizedName.value,
    tag := "v"+version.value,
    releaseName := name.value +" "+ tag.value,
    commitish := "",
    draft := false,
    // According to the Semantic Versioning Specification (rule 9)
    // a version containing a hyphen is a pre-release version
    prerelease := version.value.matches(""".*-.*"""),

    mediaTypesMap := {
      val typeMap = new javax.activation.MimetypesFileTypeMap()
      // NOTE: github doesn't know about application/java-archive type (see https://developer.github.com/v3/repos/releases/#input-2)
      typeMap.addMimeTypes("application/zip jar zip")
      // and .pom is unlikely to be in the system's default MIME types map
      typeMap.addMimeTypes("application/xml pom xml")

      typeMap.getContentType
    },

    releaseAssets := packagedArtifacts.value.values.toSeq,

    checkGithubCredentials := {
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
    },

    ghreleaseGetRepo := {
      val log = streams.value.log
      val github = checkGithubCredentials.value

      val repository = Try { github.getRepository(repo.value) } getOrElse {
        sys.error(s"Repository ${repo.value} doesn't exist or is not accessible.")
      }
      repository
    },

    // ghreleaseGetReleaseBuilder := getReleaseBuilder.value,

    releaseOnGithub := defs.releaseOnGithub.value
  )

}
