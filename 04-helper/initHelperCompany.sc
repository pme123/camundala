import mainargs.*

import $ivy.`io.github.pme123:camundala-helper_3:1.29.0-SNAPSHOT compat`, camundala.helper.setup.*
import camundala.helper.*
import camundala.helper.util.{RepoConfig, RepoCredentials}

private lazy val mycompanyCred = RepoCredentials.UserPassword(
  "mycompany",
  "bin.mycompany.com",
  "VALIANT_MVN_REPOSITORY_USERNAME",
  "VALIANT_MVN_REPOSITORY_PASSWORD"
)

private val mycompanyHost = """https://bin.mycompany.com/artifactory"""
private lazy val releaseRepo = RepoConfig.Artifactory(
  "release",
  mycompanyHost,
  "myprojects-maven-local",
  mycompanyCred.usernameEnv,
  mycompanyCred.passwordEnv
)

/** Usage see `mycompany.camundala.helper.UpdateHelper`
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
