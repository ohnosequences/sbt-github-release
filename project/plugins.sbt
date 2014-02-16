resolvers += "Era7 releases" at "http://releases.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "nice-sbt-settings" % "0.3.1")

resolvers += "Era7 snapshots" at "http://snapshots.era7.com.s3.amazonaws.com"

addSbtPlugin("ohnosequences" % "sbt-github-release" % "0.1.0-SNAPSHOT")
