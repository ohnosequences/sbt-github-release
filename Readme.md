## Sbt Github release plugin

[![](https://travis-ci.org/ohnosequences/sbt-github-release.svg?branch=master)](https://travis-ci.org/ohnosequences/sbt-github-release)
[![](https://img.shields.io/codacy/811d530bf7d548ed8bcbb506f7490bef.svg)](https://www.codacy.com/app/ohnosequences/sbt-github-release)
[![](http://img.shields.io/github/release/ohnosequences/sbt-github-release/all.svg)](https://github.com/ohnosequences/sbt-github-release/releases/latest)
[![](https://img.shields.io/badge/license-AGPLv3-blue.svg)](https://tldrlegal.com/license/gnu-affero-general-public-license-v3-%28agpl-3.0%29)
[![](https://img.shields.io/badge/contact-gitter_chat-dd1054.svg)](https://gitter.im/ohnosequences/sbt-github-release)

This is a simple sbt-plugin for creating [Github releases](https://github.com/blog/1547-release-your-software) with proper release notes and optional artifact uploading. It can be useful as a part of an [automated release process](https://github.com/ohnosequences/nice-sbt-settings).


## Usage

### SBT Dependency

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
addSbtPlugin("ohnosequences" % "sbt-github-release" % "<version>")
```

(see the latest release version on the badge above)

> Note that since `v0.5.0` this plugin is compiled and published _only_ for **sbt-1.x**. If you need it for sbt-0.13, use [`v0.4.0`](https://github.com/ohnosequences/sbt-github-release/tree/v0.4.0).

### Task keys

The main task is `githubRelease`, it creates the release and publishes the assets.

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
| `ghreleaseGithubToken`   | `Option[String]`     | OAuth credentials used to access Github API           |


You can find their defaults in the plugin [code](src/main/scala/SbtGithubReleasePlugin.scala).

#### Autodetect repository organization and name

By default, this plugin will try to auto-detect settings for `ghreleaseRepoOrg` and `ghreleaseRepoName` based on git remote with name `origin`. If such remote not exist then plugin will fallback to sbt organization/name. If you would like to avoid auto-detect behavior you should set `ghreleaseRepoOrg` and `ghreleaseRepoName` explicitly.

#### Assets

You can set which files to attach to the release using the `ghreleaseAssets` task (of `Seq[File]` type). By default it refers to the `packagedArtifacts` task.

Note, that Github requires to set the media ([MIME](https://en.wikipedia.org/wiki/Media_type)) type of each asset. You can customize which media types will be used through the `ghreleaseMediaTypesMap` setting. Github documentation refers to [IANA](https://www.iana.org/assignments/media-types/media-types.xhtml) for the list of accepted types.

By default `ghreleaseMediaTypesMap` is set to the default Java [`MimetypesFileTypeMap`](https://docs.oracle.com/javase/8/docs/api/javax/activation/MimetypesFileTypeMap.html) (with some modifications) which looks for the MIME types files in various places in your system. If you don't have any, you can download [one](http://svn.apache.org/viewvc/httpd/httpd/trunk/docs/conf/mime.types?view=co) and save it as `~/.mime.types`. If you are uploading only `.jar` and `.pom` files, you don't need to do anything.

If you don't want to upload any files, just set `GithubRelease.ghreleaseAssets := Seq()`


#### Credentials

This plugin requires an OAuth token with `repo` scope to interact with Github API. Use this link to create it in your Github profile:

* https://github.com/settings/tokens/new?description=sbt-github-release&scopes=repo

By default `ghreleaseGithubToken` looks for the token in the following places:

* `GITHUB_TOKEN` environment variable (using `githubTokenFromEnv(...)` shortcut),
* `~/.github` properties file (using `githubTokenFromFile(...)` shortcut). Expected file format is
    ```
    oauth = 623454b0sd3645bdfdes541dd1fdg34504a8cXXX
    ```

You can use either of these two shortcuts (with `import ohnosequences.sbt.GithubRelease.defs._`) or any other way to retrieve the token and set it explicitly.
