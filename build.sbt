sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.4"
sbtVersion   := "1.0.4"

bucketSuffix := "era7.com"

libraryDependencies += "org.kohsuke" % "github-api" % "1.92"

bintrayReleaseOnPublish := !isSnapshot.value
bintrayOrganization     := Some(organization.value)
bintrayPackageLabels    := Seq("sbt", "sbt-plugin", "github", "releases", "publish")

publishMavenStyle := false
publishTo := (publishTo in bintray).value
