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

|             Key | Type             | Default value                                      |
|----------------:|:-----------------|:---------------------------------------------------|
|      `notesDir` | `File`           | `notes/`                                           |
|     `notesFile` | `File`           | `<notesDir>/<version>.markdown`                    |
|          `repo` | `String`         | `"<organization>/<normalizedName>"`                |
|           `tag` | `String`         | `"v<version>"`                                     |
|   `releaseName` | `String`         | `"<name> <tag>"`                                   |
|     `commitish` | `String`         | `""` (the default repo's branch)                   |
|         `draft` | `Boolean`        | `false`                                            |
|    `prerelease` | `Boolean`        | `false` (`true` if the version has a hyphen)       |
| `releaseAssets` | `Seq[File]`      | all files from the `packagedArtifacts` setting     |
| `mediaTypesMap` | `File => String` | [media types map](#assets) for the asset artifacts |

You can change them in your `build.sbt` for example

```scala
GithubRelease.repo := "ohnosequences/sbt-github-release"

GithubRelease.draft := true
```

#### Assets

You set which files to attach to the release using the `releaseAssets` setting.

Note, that Github requires to set the media ([MIME]) type of each asset. You can customize which media types will be used through the `mediaTypesMap` setting. Github documentation refers to [IANA](https://www.iana.org/assignments/media-types/media-types.xhtml) for the list of accepted types.

By default `mediaTypesMap` is set to the default Java [`MimetypesFileTypeMap`](https://docs.oracle.com/javase/8/docs/api/javax/activation/MimetypesFileTypeMap.html) (with some modifications) which looks for the MIME types files in various places in your system. If you don't have any, you can download [one](http://svn.apache.org/viewvc/httpd/httpd/trunk/docs/conf/mime.types?view=co) and save it as `~/.mime.types`. If you are uploading only `.jar` and `.pom` files, you don't need to do anything.

If you don't want to upload any files, just set `GithubRelease.releaseAssets := Seq()`


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


[MIME]: https://en.wikipedia.org/wiki/Media_type
