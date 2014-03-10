Nice.scalaProject

sbtPlugin := true

organization := "ohnosequences"

name := "sbt-github-release"

description := "sbt plugin using github releases api"

bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"

libraryDependencies += "org.kohsuke" % "github-api" % "1.49"
