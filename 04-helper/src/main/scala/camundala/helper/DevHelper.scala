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

  def createProcess(processName: String, version: Option[Int], subProject: Option[String])(using
      config: SetupConfig
  ): Unit =
    val name = subProject.map(_ => processName).getOrElse(processName.head.toUpper + processName.tail)
    val processOrSubProject = subProject.getOrElse(processName)

    SetupGenerator().createProcess(SetupElement(
      "Process",
      processOrSubProject,
      name,
      version
    ))
  end createProcess

  def createCustomTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "CustomTask",
      processName,
      bpmnName,
      version
    ))
  def createServiceTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "ServiceTask",
      processName,
      bpmnName,
      version
    ))

  def createUserTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createUserTask(
      SetupElement("UserTask", processName, bpmnName, version)
    )

  def createDecision(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createDecision(
      SetupElement("Decision", processName, bpmnName, version)
    )

  def createSignalEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Signal",
      processName,
      bpmnName,
      version
    ))

  def createMessageEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Message",
      processName,
      bpmnName,
      version
    ))

  def createTimerEvent(processName: String, bpmnName: String, version: Option[Int])(using
      config: SetupConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Timer",
      processName,
      bpmnName,
      version
    ))

end DevHelper
