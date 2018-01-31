sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.4"
sbtVersion   := "1.0.4"

bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"
libraryDependencies += "org.kohsuke" % "github-api" % "1.89"

bintrayReleaseOnPublish := !isSnapshot.value
bintrayOrganization     := Some(organization.value)
bintrayPackageLabels    := Seq("sbt", "sbt-plugin", "github", "releases", "publish")

publishMavenStyle := false
publishTo := (publishTo in bintray).value

// Publishing fat-jar
artifact in (Compile, assembly) ~= { _.withClassifier(Some("fat")) }
addArtifact(artifact in (Compile, assembly), assembly)
