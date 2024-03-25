package camundala.helper

import camundala.helper.setup.*
import camundala.helper.util.RepoConfig

object DevHelper:

  // company (./helperCompany.sc)
  def createUpdateCompany(companyName: String, repoConfig: RepoConfig.Artifactory) =
    println(s"Create/Update Company: $companyName")
    given config: SetupConfig = SetupCompanyGenerator.init(companyName, repoConfig)
    SetupCompanyGenerator().generate
  end createUpdateCompany

  // project (./projects/helperProject.sc)
  def updateProject(config: HelperConfig): Unit =
    println(s"Update Project: ${config.setupConfig.projectName}")
    SetupGenerator()(using config.setupConfig).generate

  // project (./projects/mycompany-myproject/helper.sc)
  def update(using config: SetupConfig): Unit =
    println(s"Update Project: ${config.projectName}")
    println(s" - with Subprojects: ${config.subProjects}")
    SetupGenerator().generate

  def createProcess(processName: String, version: Option[Int])(using config: SetupConfig): Unit =
    println(s"Create Process: $processName v$version in ${config.projectName}")
    SetupGenerator().createProcess(processName, version)

  def createCustomTask(processName: String, workerName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "CustomTask",
      processName,
      workerName,
      version
    ))

  def createServiceTask(processName: String, workerName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "ServiceTask",
      processName,
      workerName,
      version
    ))

  def createUserTask(processName: String, workerName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createUserTask(
      SetupElement("UserTask", processName, workerName, version)
    )

  def createSignalEvent(processName: String, workerName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Signal",
      processName,
      workerName,
      version
    ))

  def createMessageEvent(processName: String, workerName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Message",
      processName,
      workerName,
      version
    ))

  def createTimerEvent(processName: String, workerName: String, version: Option[Int])(using
      config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Timer",
      processName,
      workerName,
      version
    ))

end DevHelper
