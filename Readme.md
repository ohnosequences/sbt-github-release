## Sbt Github release plugin

This is a simple sbt-plugin for creating [Github releases](https://github.com/blog/1547-release-your-software) with proper release notes and optional artifact uploading using [kohsuke/github-api](https://github.com/kohsuke/github-api) library.

It can be useful as a part of [automated release process](https://github.com/sbt/sbt-release).


### Features

* Optionally publishes release notes
* Optionally uploads files


## Usage

### SBT Dependency

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.1.0")
```

> **Note:** you should use sbt `v0.13.+`


### Setting keys

Most of these keys just reflect the [parameters from Github API](http://developer.github.com/v3/repos/releases/#create-a-release):

Key           | Type        | Default value
-------------:|:------------|:--------------------------------------------------------
`notesDir`    | `File`      | `notes/`
`notesFile`   | `File`      | `<notesDir>/<version>.markdown`
`repo`        | `String`    | `"<organization>/<normalizedName>"`
`tag`         | `String`    | `"v<version>"`
`releaseName` | `String`    | `"<name> <tag>"`
`commitish`   | `String`    | `""` (the default repo's branch)
`draft`       | `Boolean`   | `false`
`prerelease`  | `Boolean`   | `false` (`true` if the version has a milestone suffix)
`assets`      | `Seq[File]` | `Seq(<packageBin in Compile>)`

So you can add to your `build.sbt` for example

```scala
GithubRelease.defaults

GithubRelease.repo := "ohnosequences/sbt-github-release"

GithubRelease.draft := true
```


### Task keys

* `checkGithubCredentials` — checks Github OAuth token and help to set it if needed
* `releaseOnGithub` — the main task, which creates a release and publishes the assests


### Credentials

This plugin requires an OAuth token from your Github account. It should be placed in `~/.github`:

```
oauth = 623454b0sd3645bdfdes541dd1fdg34504a8cXXX
```

But you don't need to create it manually — when running `releaseOnGithub`, plugin checks this file and if there is no valid token, asks you to go and create one and then saves it.


### Intergation with sbt-release

`TODO`
