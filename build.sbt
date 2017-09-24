sbtPlugin := true

name         := "sbt-github-release"
organization := "ohnosequences"
description  := "sbt plugin using github releases api"

scalaVersion := "2.12.3"
sbtVersion   := "1.0.2"

bucketSuffix := "era7.com"

libraryDependencies += "org.kohsuke" % "github-api" % "1.77"

libraryDependencies := libraryDependencies.value
  .filterNot { _.name == "scalatest" }

wartremoverErrors in (Compile, compile) --= Seq(Wart.Any, Wart.NonUnitStatements)
