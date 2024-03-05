package camundala.helper.setup

import camundala.helper.util.{CompanyVersionHelper, ReposConfig}

case class HelperCreator(companyName: String)(using reposConfig: ReposConfig):

  lazy val companyHelper: String =
    s"""$helperHeader
       |
       |/**
       | * Usage see `valiant.camundala.helper.SetupHelper#createCompany`
       | */
       |@main(doc =
       |  \"\"\"> Creates the directories and generic files for the company BPMN Projects
       |   \"\"\")
       |def create(
       |    @arg(doc = "The company name - should be generated automatically after creation.")
       |    companyName: String = $companyName,
       |): Unit =
       |  SetupHelper().createCompany(companyName)
       |
       |""".stripMargin

  def projectHelper(projectName: String) =
    s"""$helperHeader
       |/**
       | * Usage see `valiant.camundala.helper.PublishHelper`
       | */
       |
       |@main(doc = "> Creates a new Release for the client and publishes to bpf-generic-release")
       |def publish(
       |             @arg(doc = "The Version you want to publish.")
       |             version: String): Unit = {
       |  PublishHelper().publish(version)
       |}
       |
       |/**
       | * Usage see `valiant.camundala.helper.DeployHelper`
       | */
       |@main(doc = "> Publishes Local (sbt publishLocal) and runs the deploy_manifest of Postman")
       |def deploy(
       |            @arg(doc = "Optional Gatling Test that is run after deployment.")
       |            integrationTest: Option[String] = None): Unit = {
       |  DeployHelper().deploy(integrationTest)
       |}
       |
       |/**
       | * Usage see `valiant.camundala.helper.UpdateHelper`
       | */
       |@main(doc =
       |  \"\"\"> Updates your Project with latest versions and also updates generic files, that starts with '$doNotAdjust'.
       |      - set in `helper.sc` the version you want: $helperImport
       |   \"\"\")
       |def update(
       |    @arg(doc = "The project name - should be generated automatically after creation.")
       |    projectName: String = "$projectName",
       |    @arg(doc = "SubProjects - should be generated automatically after creation.")
       |    subProjects: Seq[String] = Seq.empty
       |): Unit =
       |  UpdateHelper().update(projectName, subProjects)
       |
       |/**
       | * Usage see `valiant.camundala.helper.DockerHelper`
       | */
       |@main(doc = "> Starts your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerUp(): Unit = {
       |  DockerHelper().dockerUp()
       |}
       |
       |/**
       | * Usage see `valiant.camundala.helper.DockerHelper`
       | */
       |@main(doc = "> Stops your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerStop(): Unit = {
       |  DockerHelper().dockerStop()
       |}
       |
       |/**
       | * Usage see `valiant.camundala.helper.DockerHelper`
       | */
       |@main(doc = "> Stops and removes your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerDown(): Unit = {
       |  DockerHelper().dockerDown()
       |}
       |""".stripMargin

  private lazy val versionHelper = CompanyVersionHelper(companyName, reposConfig.repoSearch)
  private lazy val helperImport =
    s"""import $$ivy.`$companyName:$companyName-camundala-helper_3:${versionHelper.companyCamundalaVersion} compat`"""
  private lazy val helperHeader =
    s"""/* $doNotAdjust. This file is replaced by `amm helper.sc update`.
       |
       |* ONLY CHANGE valiant-camundala-helper VERSION:
       |* $helperImport // <-- VERSION
       |* This file is replaced by `amm helper.sc update` of the defined version.
       |**/
       |import mainargs._
       |import coursierapi.{Credentials, MavenRepository}
       |
       |interp.repositories() ++= Seq(
       |  ${reposConfig.ammoniteRepos
        .mkString(",\n  ")}
       |)
       |@
       |
       |$helperImport, $companyName.camundala.helper._
       |""".stripMargin
end HelperCreator
