package ohnosequences.sbt

import sbt._
import Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._

object SbtGithubReleasePlugin extends sbt.Plugin {

  object GithubRelease {

    // Setting keys:
    lazy val notesDir = settingKey[File]("Directory with release notes")
    lazy val notesFile = settingKey[File]("File with the release notes")
    lazy val repo = settingKey[String]("org/repo")
    lazy val tag = settingKey[String]("The name of the tag: vX.Y.Z")
    lazy val name = settingKey[String]("The name of the release")
    // lazy val commitish = settingKey[String]("Specifies the commitish value that determines where the Git tag is created from")
    lazy val body = settingKey[String]("Text describing the contents of the tag")
    lazy val draft = settingKey[Boolean]("true to create a draft (unpublished) release, false to create a published one")
    lazy val prerelease = settingKey[Boolean]("true to identify the release as a prerelease. false to identify the release as a full release")


    lazy val releaseOnGithub = taskKey[Unit]("")

    lazy val settings: Seq[Setting[_]] = Seq(
      notesDir := file(baseDirectory.value + "/notes/"),
      notesFile := notesDir.value / (version.value+".markdown"),
      repo := organization.value +"/"+ name.value,
      tag := "v"+version.value,
      name := name.value +" "+ tag.value,
      // commitish := "master",
      body := IO.read(notesFile.value),
      draft := false,
      prerelease := false,

      releaseOnGithub := {
        val s: TaskStreams = streams.value

        val github = GitHub.connectUsingOAuth("66844eb0ff3693b49ae1d073b1f533c644a8c79e");

        s.log.info(s"Releasing on Github at ${repo.value} ${tag.value}")

        github.getRepository(repo.value).
          createRelease(tag.value).
          body(body.value).
          name(name.value).
          draft(draft.value).
          prerelease(prerelease.value).
          create
      }
    )

  }

}
