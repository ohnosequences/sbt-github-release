sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.7"
sbtVersion   := "1.2.6"

//bucketSuffix := "era7.com"

libraryDependencies += "org.kohsuke" % "github-api" % "1.92"
