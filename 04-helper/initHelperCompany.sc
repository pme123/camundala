
import mainargs._

import $ivy.`io.github.pme123:camundala-helper_3:1.29.0-SNAPSHOT compat`, camundala.helper.setup._
import camundala.helper.*
import camundala.helper.util.{RepoConfig, RepoCredentials}

private lazy val swisscomCred = RepoCredentials.UserPassword(
  "swisscom",
  "bin.swisscom.com",
  "VALIANT_MVN_REPOSITORY_USERNAME",
  "VALIANT_MVN_REPOSITORY_PASSWORD"
)

private val swisscomHost = """https://bin.swisscom.com/artifactory"""
private lazy val releaseRepo = RepoConfig.Artifactory(
  "release",
  swisscomHost,
  "valiantagilefactorybpfpkgvaliantreleases-maven-local",
  swisscomCred.usernameEnv,
  swisscomCred.passwordEnv
)

/** Usage see `valiant.camundala.helper.UpdateHelper`
  */
@main(doc =
  """> Creates the directories and generic files for the company BPMN Projects
   """
)
def init(
    @arg(doc = "The company name - should be generated automatically after creation.")
    companyName: String
): Unit =
  DevHelper.createUpdateCompany(companyName, releaseRepo)
