
```scala
package ohnosequences.sbt

import sbt._
import Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._

object SbtGithubReleasePlugin extends sbt.Plugin {

  object GithubRelease {

    // Setting keys:
    lazy val notesDir = settingKey[File]("Directory with release notes")
    lazy val notesFile = settingKey[File]("File with the release notes for the current version")
    lazy val repo = settingKey[String]("org/repo")
    lazy val tag = settingKey[String]("The name of the tag: vX.Y.Z")
    lazy val releaseName = settingKey[String]("The name of the release")
    lazy val commitish = settingKey[String]("Specifies the commitish value that determines where the Git tag is created from")
    lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")
    lazy val prerelease = settingKey[Boolean]("true to identify the release as a prerelease. false to identify the release as a full release")
    lazy val assets = taskKey[Seq[File]]("The files to upload")

    lazy val checkGithubCredentials = taskKey[GitHub]("Checks authentification and suggests to create a new oauth token if needed")
    lazy val releaseOnGithub = taskKey[GHRelease]("Publishes a release of Github")


    // Defaults:
    lazy val defaults: Seq[Setting[_]] = Seq(
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
        log.info("Checked Github credentials")
        GitHub.connect
      },

      releaseOnGithub := {
        if (tag.value.endsWith("-SNAPSHOT"))
          sys.error("You shouldn't publish snapshots, maybe you forgot to set correct version")

        val text = IO.read(notesFile.value)
        if (text.isEmpty)
          sys.error(s"Release notes file ${notesFile.value} is empty")

        val github = checkGithubCredentials.value
        val log = streams.value.log
        log.info(s"Releasing ${releaseName.value} ${tag.value}...")

        val release = {
          val r = github.getRepository(repo.value).
          createRelease(tag.value).
          body(text).
          name(releaseName.value).
          draft(draft.value).
          prerelease(prerelease.value)
          if (commitish.value.isEmpty) r else r.commitish(commitish.value)
        }.create

        if (release != null)
          log.info(s"Github release ${release.getName} is published" + 
            (if(draft.value) " (as a draft)" else ""))

        assets.value foreach { asset =>
          release.uploadAsset(asset, "application/zip")
          log.info(s"File ${asset} is uploaded to Github")
        }

        release
      }
    )

  }

}

```


------

### Index

+ src
  + main
    + scala
      + [SbtGithubReleasePlugin.scala][main/scala/SbtGithubReleasePlugin.scala]

[main/scala/SbtGithubReleasePlugin.scala]: SbtGithubReleasePlugin.scala.md