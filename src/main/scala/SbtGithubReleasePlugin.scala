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
    ghreleaseIsPrerelease := { _.matches(""".*-.*""") },

    ghreleaseMediaTypesMap := {
      val typeMap = new javax.activation.MimetypesFileTypeMap()
      // NOTE: github doesn't know about application/java-archive type (see https://developer.github.com/v3/repos/releases/#input-2)
      typeMap.addMimeTypes("application/zip jar zip")
      // and .pom is unlikely to be in the system's default MIME types map
      typeMap.addMimeTypes("application/xml pom xml")

      typeMap.getContentType
    },

    ghreleaseAssets := packagedArtifacts.value.values.toSeq,

    ghreleaseGetCredentials := {
      val log = streams.value.log
      val conf = file(System.getProperty("user.home")) / ".github"
      while (!conf.exists || !GitHub.connect.isCredentialValid) {
        log.warn("Your github credentials for sbt-github-release plugin are not set yet")
        SimpleReader.readLine("Go to https://github.com/settings/applications \ncreate a new token and paste it here: ") match {
          case Some(token) => {
            try {
              val gh = GitHub.connectUsingOAuth(token)
              if (gh.isCredentialValid) {
                IO.writeLines(conf, Seq("oauth = " + token))//, append = true)
                log.info("Wrote OAuth token to " + conf)
              }
            } catch {
              case e: Exception => log.error(e.toString)
            }
          }
          case _ => sys.error("If you want to use sbt-github-release plugin, you should set credentials correctly")
        }
      }
      log.info("Github credentials are ok")
      GitHub.connect
    },

    ghreleaseGetRepo := {
      val log = streams.value.log
      val github = ghreleaseGetCredentials.value
      val repo = s"${ghreleaseRepoOrg.value}/${ghreleaseRepoName.value}"

      val repository = Try { github.getRepository(repo) } getOrElse {
        sys.error(s"Repository ${repo} doesn't exist or is not accessible.")
      }
      repository
    },

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
