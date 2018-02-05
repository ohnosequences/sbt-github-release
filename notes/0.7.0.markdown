* #20: Improved credentials setting (by @EmilDafinov):
    + replaced `ghreleaseGetCredentials` task with the `ghreleaseGithubToken` setting
    + it allows credentials to be specified in the build file (retrieved from anywhere)
    + added shortcuts for getting the token from environment variable or from a property file
* #25: Fix requires in the plugin (by @jvican)
