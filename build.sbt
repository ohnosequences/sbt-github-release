Nice.scalaProject

sbtPlugin := true

name := "sbt-github-release"

description := "sbt plugin using github releases api"

organization := "ohnosequences"

bucketSuffix := "era7.com"

libraryDependencies += "org.kohsuke" %% "github-api" % "1.49"
