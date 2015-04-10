Nice.scalaProject

name := "sbt-github-release"
organization := "ohnosequences"
description := "sbt plugin using github releases api"

sbtPlugin := true
scalaVersion := "2.10.5"
bucketSuffix := "era7.com"

resolvers += "Github-API" at "http://repo.jenkins-ci.org/public/"
libraryDependencies += "org.kohsuke" % "github-api" % "1.49"

// libraryDependencies += "com.github.xuwei-k" %% "ghscala" % "0.2.14"
