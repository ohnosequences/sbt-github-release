This release brings major (breaking) changes.

* #13: Added `ghreleaseMediaTypesMap` setting for guessing MIME type of the release assets (see also #10, #11)
* #8 & #15: Changed all setting/task keys. Now all of them are prefixed with `ghrelease` except the main task, which is now called `githubRelease`.

----

(Hopefully) full list of changes in settings/tasks:

* These keys have changed (`TagName` is just an alias for `String`):
  - `repo` is now split in 2 parts: `ghreleaseRepoOrg` and `ghreleaseRepoName`
  - `notesFile` is now `ghreleaseNotes` and has type `TagName => String` (you can read it from file or somewhere else)
  - `releaseName` is now `ghreleaseTitle` and has type `TagName => String`
  - `prerelease` is now `ghreleaseIsPrerelease` of type `TagName => Boolean`
* These keys have been removed:
  - `tag`: now the `githubRelease` task _takes tag name as a parameter_
  - `notesDir`
  - `commitish`
  - `draft`
* These tasks have been changed/added:
  - `releaseAssets` is now `ghreleaseAssets`
  - `checkGithubCredentials` is now `ghreleaseGetCredentials`
  - `ghreleaseGetRepo` and `ghreleaseGetReleaseBuilder` intermediate tasks were added
  - `releaseOnGithub` is now an _input task_ `githubRelease`, it takes tag name as an argument:
    + it can be used interactively in sbt and will autocomplete existing git tags
    + it can be used non-interactively through `ohnosequences.sbt.GithubRelease.defs.githubRelease("<tag name>")`
