sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.3"
sbtVersion   := "1.0.2"

bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"
libraryDependencies += "org.kohsuke" % "github-api" % "1.89"

libraryDependencies := libraryDependencies.value
  .filterNot { _.name == "scalatest" }

wartremoverErrors in (Compile, compile) --= Seq(Wart.Any, Wart.NonUnitStatements)

bintrayReleaseOnPublish := !isSnapshot.value
bintrayOrganization     := Some(organization.value)
bintrayPackageLabels    := Seq("sbt", "sbt-plugin", "github", "releases", "publish")

publishMavenStyle := false
publishTo := (publishTo in bintray).value
