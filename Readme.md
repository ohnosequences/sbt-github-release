## Sbt Github release plugin

This is a simple sbt-plugin for creating [Github releases](https://github.com/blog/1547-release-your-software) with proper release notes and optional artifact uploading using [kohsuke/github-api](https://github.com/kohsuke/github-api) library.

It can be useful as a part of [automated release process](https://github.com/sbt/sbt-release).


### Features

* Publishes release notes
* Optionally uploads jar assets
* Exposes all available parameters of the Github releases API


## Usage

### SBT Dependency

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"

addSbtPlugin("ohnosequences" % "sbt-github-release" % "<version>")
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
`prerelease`  | `Boolean`   | `false` (`true` if the version has a hyphen)
`assets`      | `Seq[File]` | `Seq(<packageBin in Compile>)`

You can change them in your `build.sbt` for example

```scala
GithubRelease.repo := "ohnosequences/sbt-github-release"

GithubRelease.draft := true
```

If you don't want to upload any files, just set `GithubRelease.assests := Seq()`


### Task keys

* `checkGithubCredentials` — checks Github OAuth token and helps to set it if needed
* `releaseOnGithub` — the main task, which creates a release and publishes the assests


### Credentials

This plugin requires an OAuth token from your Github account. It should be placed in `~/.github`:

```
oauth = 623454b0sd3645bdfdes541dd1fdg34504a8cXXX
```

But you don't need to create this file manually — when running `releaseOnGithub`, plugin checks it and if there is no valid token, asks you to go and create one and then saves it.


### Integration with sbt-release

See how it's done in the [nice-sbt-settings](https://github.com/ohnosequences/nice-sbt-settings/blob/master/src/main/scala/ReleaseSettings.scala#L277-L290) plugin for an example.


## Contacts

This project is maintained by [@laughedelic](https://github.com/laughedelic). Join the chat-room if you want to ask or discuss something  
[![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/ohnosequences/sbt-github-release?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
