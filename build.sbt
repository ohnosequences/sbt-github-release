import sbtrelease._
import ReleaseKeys._
import ReleaseStateTransformations._


Nice.scalaProject

sbtPlugin := true

organization := "ohnosequences"

name := "sbt-github-release"

description := "sbt plugin using github releases api"

bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"

libraryDependencies += "org.kohsuke" % "github-api" % "1.+"


GithubRelease.defaults

GithubRelease.repo := "ohnosequences/sbt-github-release"

GithubRelease.draft := true

lazy val checkGHCredsStep = ReleaseStep({st => Project.extract(st).runTask(GithubRelease.checkGithubCredentials, st)._1 })
lazy val githubReleaseStep = ReleaseStep({st => Project.extract(st).runTask(GithubRelease.releaseOnGithub, st)._1 })

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  checkGHCredsStep, ///
  inquireVersions,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  githubReleaseStep, ///
  setNextVersion,
  commitNextVersion,
  pushChanges
)
