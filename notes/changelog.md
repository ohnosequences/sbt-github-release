* #13: Added `ghreleaseMediaTypesMap` setting for guessing MIME type of the release assets (see also #10, #11)
* #15: Renamed setting/task keys. Now all of them are prefixed with `ghrelease` except the main task, which is now called `githubRelease`:
  - `notesFile` is now `ghreleaseNotes`
  - `repo` is now split in 2 parts: `ghreleaseRepoOrg` and `ghreleaseRepoName`
  - `releaseName` is now `ghreleaseTitle`
  - `commitish` is now `ghreleaseCommitish`
  - `tag` is now `ghreleaseTag`
  - `releaseAssets` task is now `ghreleaseAssets`
  - `notesDir` key is removed
  - `prerelease` is now `ghreleaseIsPrerelease` of type `String => Boolean`
  - `draft` is temporarily removed
