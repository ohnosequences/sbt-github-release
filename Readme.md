## Sbt Github release plugin

[![](https://travis-ci.org/ohnosequences/sbt-github-release.svg?branch=master)](https://travis-ci.org/ohnosequences/sbt-github-release)
[![](https://img.shields.io/codacy/811d530bf7d548ed8bcbb506f7490bef.svg)](https://www.codacy.com/app/ohnosequences/sbt-github-release)
[![](http://github-release-version.herokuapp.com/github/ohnosequences/sbsbt-github-release/release.svg)](https://github.com/ohnosequences/sbt-github-release/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/sbt-github-release)

This is a simple sbt-plugin for creating [Github releases](https://github.com/blog/1547-release-your-software) with proper release notes and optional artifact uploading. It can be useful as a part of an [automated release process](https://github.com/ohnosequences/nice-sbt-settings).


## Usage

### SBT Dependency

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "https://s3-eu-west-1.amazonaws.com/releases.era7.com"
resolvers += "Jenkins repo" at "http://repo.jenkins-ci.org/public/"

addSbtPlugin("ohnosequences" % "sbt-github-release" % "<version>")
```

> **Note:** you should use sbt `v0.13.+`


### Setting keys

| Key                      | Type                 | Short description                                     |
|:-------------------------|:---------------------|:------------------------------------------------------|
| `ghreleaseRepoOrg`       | `String`             | Github repository organization                        |
| `ghreleaseRepoName`      | `String`             | Github repository name                                |
| `ghreleaseNotes`         | `TagName => String`  | Release notes for the given tag                       |
| `ghreleaseTitle`         | `TagName => String`  | The title of the release                              |
| `ghreleaseIsPrerelease`  | `TagName => Boolean` | A function to determine release as a prerelease based |
| `ghreleaseAssets`        | `Seq[File]`          | The artifact files to upload                          |
| `ghreleaseMediaTypesMap` | `File => String`     | A function to determine media type for the assets     |


You can find their defaults in the plugin [code](src/main/scala/SbtGithubReleasePlugin.scala.md).


#### Assets

You can set which files to attach to the release using the `ghreleaseAssets` task (of `Seq[File]` type). By default it refers to the `packagedArtifacts` task.

Note, that Github requires to set the media ([MIME](https://en.wikipedia.org/wiki/Media_type)) type of each asset. You can customize which media types will be used through the `ghreleaseMediaTypesMap` setting. Github documentation refers to [IANA](https://www.iana.org/assignments/media-types/media-types.xhtml) for the list of accepted types.

By default `ghreleaseMediaTypesMap` is set to the default Java [`MimetypesFileTypeMap`](https://docs.oracle.com/javase/8/docs/api/javax/activation/MimetypesFileTypeMap.html) (with some modifications) which looks for the MIME types files in various places in your system. If you don't have any, you can download [one](http://svn.apache.org/viewvc/httpd/httpd/trunk/docs/conf/mime.types?view=co) and save it as `~/.mime.types`. If you are uploading only `.jar` and `.pom` files, you don't need to do anything.

If you don't want to upload any files, just set `GithubRelease.ghreleaseAssets := Seq()`


### Task keys

The main task is `githubRelease`, it creates the release and publishes the assests.

There are some other tasks which work as intermediate checks:

* `ghreleaseGetCredentials` — checks Github OAuth token and helps to set it if needed
* `ghreleaseGetRepo` — checks that the repository exists and is accessible
* `ghreleaseGetReleaseBuilder` — checks that Github repo contains the tag and there is no release based on it yet


### Credentials

This plugin requires an OAuth token from your Github account. It should be placed in `~/.github`:

```
oauth = 623454b0sd3645bdfdes541dd1fdg34504a8cXXX
```

But you don't need to create this file manually — when running `githubRelease`, plugin checks it and if there is no valid token, asks you to go and create one and then saves it.
