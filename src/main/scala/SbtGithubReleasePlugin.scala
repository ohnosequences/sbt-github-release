package ohnosequences.sbt

import sbt._, Keys._

import org.kohsuke.github._
import scala.collection.JavaConversions._
import scala.util.Try
import sbt._, Keys._, complete._, DefaultParsers._

case object SbtGithubReleasePlugin extends AutoPlugin {

  // This plugin will load automatically
  override def trigger = allRequirements

  val autoImport = GithubRelease.keys

  import GithubRelease._, keys._

  // Default settings
  override lazy val projectSettings = Seq[Setting[_]](
    ghreleaseNotes     := baseDirectory.value / "notes" / (version.value+".markdown"),
    ghreleaseRepoOrg   := organization.value,
    ghreleaseRepoName  := name.value,
    ghreleaseTitle     := { tagName => s"${name.value} ${tagName}" },
    // According to the Semantic Versioning Specification (rule 9)
    // a version containing a hyphen is a pre-release version
    ghreleaseIsPrerelease   := { _.matches(""".*-.*""") },
    ghreleaseMediaTypesMap  := defs.ghreleaseMediaTypesMap,
    ghreleaseAssets         := packagedArtifacts.value.values.toSeq,
    ghreleaseGetCredentials := defs.ghreleaseGetCredentials.value,
    ghreleaseGetRepo        := defs.ghreleaseGetRepo.value,

    ghreleaseGetReleaseBuilder := Def.inputTaskDyn {
      defs.ghreleaseGetReleaseBuilder(tagNameArg.parsed)
    }.evaluated,

    githubRelease := Def.inputTaskDyn {
      defs.githubRelease(tagNameArg.parsed)
    }.evaluated
  )

  def tagNameArg: Parser[String] = {
    // TODO: suggest existing local git tags
    Space ~> StringBasic
  }
}
