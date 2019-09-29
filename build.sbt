sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.10"
sbtVersion   := "1.3.2"

bucketSuffix := "era7.com"

libraryDependencies += "org.kohsuke" % "github-api" % "1.92"
