package ohnosequences.sbt

import sbt._
import Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._

object SbtGithubReleasePlugin extends AutoPlugin {

  object autoImport {
    // this object is just as a namespace:
    object GithubRelease {
      lazy val notesDir = settingKey[File]("Directory with release notes")
      lazy val notesFile = settingKey[File]("File with the release notes for the current version")
      lazy val repo = settingKey[String]("org/repo")
      lazy val tag = settingKey[String]("The name of the tag: vX.Y.Z")
      lazy val releaseName = settingKey[String]("The name of the release")
      lazy val commitish = settingKey[String]("Specifies the commitish value that determines where the Git tag is created from")
      lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")
      lazy val prerelease = settingKey[Boolean]("true to identify the release as a prerelease. false to identify the release as a full release")
      lazy val assets = taskKey[Seq[File]]("The files to upload")
    }

    lazy val checkGithubCredentials = taskKey[GitHub]("Checks authentification and suggests to create a new oauth token if needed")
    lazy val releaseOnGithub = taskKey[GHRelease]("Publishes a release of Github")
  }
  import autoImport._, GithubRelease._


  // This plugin will load automatically
  override def trigger = allRequirements

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

    assets := Seq((packageBin in Compile).value),

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

    releaseOnGithub := {
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

      val github = checkGithubCredentials.value

      val release = {
        val r = github.getRepository(repo.value).
        createRelease(tag.value).
        body(text).
        name(releaseName.value).
        draft(draft.value).
        prerelease(prerelease.value)
        if (commitish.value.isEmpty) r else r.commitish(commitish.value)
      }.create

      if (release != null) {
        val pre = if (prerelease.value) "pre-" else ""
        val pub = if(draft.value) "saved as a draft" else "published"
        log.info(s"Github ${pre}release '${release.getName}' is ${pub} at\n  ${release.getHtmlUrl}")
      } else sys.error("Something went wrong with Github release")

      assets.value foreach { asset =>
        release.uploadAsset(asset, "application/zip")
        val rel = asset.relativeTo(baseDirectory.value).getOrElse(asset)
        log.info(s"Asset [${rel}] is uploaded to Github")
      }

      release
    }
  )

}
