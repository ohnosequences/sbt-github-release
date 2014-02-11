## Sbt Github release plugin

This is a simple sbt-plugin for creating [Github releases](https://github.com/blog/1547-release-your-software) with proper release notes and optional artifact uploading using [kohsuke/github-api](https://github.com/kohsuke/github-api) library.

It can be useful as a part of [automated release process](https://github.com/sbt/sbt-release).


## Usage


### Dependency

To start using this plugin add the following to the `project/plugins.sbt`:

```scala
resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.1.0")
```

> **Note**: you should use sbt `v0.13.+`.


### Setting keys

Key           | Type    | Default value
-------------:|:--------|:-----------------------------------------------------------------
`notesDir`    | File    | `notes/`
`notesFile`   | File    | `notes/<version>.markdown`
`repo`        | String  | `"<organization>/<normalizedName>"`
`tag`         | String  | `"v<version>"`
`releaseName` | String  | `"<name> <tag>"`
`commitish`   | String  | `""` (the default repo's branch)
`draft`       | Boolean | `false`
`prerelease`  | Boolean | `false` (if your version doesn't have milestone suffix)
`uploadAsset` | Boolean | `false`
`asset`       | File    | `<packageBin in Compile>`

So you can add to your `build.sbt` for example

```
GithubRelease.defaults

GithubRelease.repo := "myorg/myrepo"

GithubRelease.draft := true

GithubRelease.uploadAsset := true
```

Then you can use the `releaseOnGithub` task to create a release on Github.


### Credentials

This plugin requires an OAuth token from your Github account. It should be placed in `~/.github`:

```
oauth = 623454b0sd3645bdfde4547ddffdg33554a8cXXX
```

But you don't need to create it manually — when running `releaseOnGithub`, plugin checks this file and if there is no valid token, asks you to go and create one and then saves it.


### Intergation with sbt-release

`TODO`
