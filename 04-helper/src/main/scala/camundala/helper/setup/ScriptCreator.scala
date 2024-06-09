package camundala.helper.setup

import camundala.helper.util.CompanyVersionHelper

case class ScriptCreator()(using config: SetupConfig):

  lazy val companyCreate =
    s"""$helperHeader
       |
       |@main(doc =
       |  \"\"\"> Creates the directories and generic files for the company BPMN Projects
       |   \"\"\")
       |def update(
       |): Unit =
       |  DevHelper.updateCompany()
       |
       |""".stripMargin

  lazy val projectCreate: String =
    s"""$helperHeader
       |
       |@main(doc =
       |  \"\"\"> Creates the directories and generic files for the company BPMN Projects
       |   \"\"\")
       |def create(
       |    @arg(doc = "The project name - should be generated automatically after creation.")
       |    projectName: String
       |): Unit = {
       |  val config = ProjectDevHelper.config(projectName)
       |  DevHelper.createProject(config)
       |}
       |""".stripMargin

  lazy val projectHelper =
    val projectName = config.projectName
    s"""$helperHeader
       |
       |lazy val projectName: String = "$projectName"
       |lazy val subProjects = Seq(
       |  ${config.subProjects.map(sp => s"\"$sp\"").mkString(", ")}
       |)
       |lazy val config: HelperConfig = ProjectDevHelper.config(projectName, subProjects)
       |given setup.SetupConfig =  config.setupConfig
       |
       |@main(doc =
       |  \"\"\"> Updates your Project with latest versions and also updates generic files, that starts with '$doNotAdjust'.
       |      - set in `helper.sc` the version you want: $helperImport
       |   \"\"\")
       |def update(): Unit = DevHelper.update
       |
       |@main(doc = "> Creates everything for a Process (bpmn, simulation, worker)")
       |def process(
       |             @arg(doc = "The name of the process (lowerCase, e.g. mySuperProcess).")
       |             processName: String,
       |             @arg(doc = "The version of the object to create.")
       |             version: Option[Int] = None,
       |             @arg(doc = "The name of the subProject (lowerCase, e.g. mySubProject).")
       |             subProject: Option[String] = None
       |
       |           ): Unit =
       |  DevHelper.createProcess(processName, version, subProject)
       |
       |${createMethod("CustomTask")}
       |
       |${createMethod("ServiceTask")}
       |
       |${createMethod("UserTask")}
       |
       |${createMethod("Decision")}
       |
       |${createMethod("SignalEvent")}
       |
       |${createMethod("MessageEvent")}
       |
       |${createMethod("TimerEvent")}
       |
       |@main(doc = "> Creates a new Release for the client and publishes to bpf-generic-release")
       |def publish(
       |             @arg(doc = "The Version you want to publish.")
       |             version: String): Unit = {
       |  PublishHelper().publish(version)
       |}
       |
       |@main(doc = "> Publishes Local (sbt publishLocal) and runs the deploy_manifest of Postman")
       |def deploy(
       |            @arg(doc = "Optional Gatling Test that is run after deployment.")
       |            integrationTest: Option[String] = None): Unit = {
       |  DeployHelper().deploy(integrationTest)
       |}
       |
       |
       |@main(doc = "> Starts your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerUp(
       |             @arg(doc = "The Version you want to publish.")
       |             imageVersion: Option[String] = None
       |  ): Unit = {
       |  DockerHelper().dockerUp(imageVersion)
       |}
       |
       |@main(doc = "> Stops your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerStop(): Unit = {
       |  DockerHelper().dockerStop()
       |}
       |
       |@main(doc = "> Stops and removes your development Docker - this assumes the Docker Files in `../../docker`.")
       |def dockerDown(): Unit = {
       |  DockerHelper().dockerDown()
       |}
       |""".stripMargin
  end projectHelper

  private lazy val companyName = config.companyName
  private lazy val reposConfig = config.reposConfig
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
       |  ${
        reposConfig.ammoniteRepos
          .map:
            _.ammoniteRepo
          .mkString(",\n  ")
      }
       |)
       |@
       |
       |$helperImport
       |import $companyName.camundala.helper._
       |import camundala.helper._
       |""".stripMargin

  private def createMethod(objectType: String) =
    val objectName = objectType.head.toLower + objectType.tail
    s"""@main(doc = "> Creates everything needed for a $objectType (e.g. bpmn, simulation, worker)")
       |def $objectName(
       |             @arg(doc = "The name of the process.")
       |             processName: String,
       |             @arg(doc = "The domain name of the object to create.")
       |             ${objectName}Name: String,
       |             @arg(doc = "The version of the object to create.")
       |             version: Option[Int] = None,
       |             ): Unit =
       |  DevHelper.create${objectType}(processName, ${objectName}Name, version)""".stripMargin
  end createMethod
end ScriptCreator
