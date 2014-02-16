Nice.scalaProject

sbtPlugin := true

name := "sbt-github-release"

description := "sbt plugin using github releases api"

organization := "ohnosequences"

bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"

libraryDependencies += "org.kohsuke" % "github-api" % "1.+"


GithubRelease.defaults

GithubRelease.repo := "ohnosequences/sbt-github-release"

GithubRelease.draft := true
