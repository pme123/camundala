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

  def createProcess(processName: String)(using config: SetupConfig): Unit =
    println(s"Create Process: $processName in ${config.projectName}")
    SetupGenerator().createProcess(processName)

  def createCustomTask(processName: String, workerName: String)(using config: SetupConfig): Unit =
    print("Custom Task", config.projectName, processName, workerName)
    SetupGenerator().createCustomTask(processName, workerName)

  def createServiceTask(processName: String, workerName: String)(using
      config: SetupConfig
  ): Unit =
    print("Service Task", config.projectName, processName, workerName)
    SetupGenerator().createServiceTask(processName, workerName)

  def createUserTask(processName: String, workerName: String)(using config: SetupConfig): Unit =
    print("User Task", config.projectName, processName, workerName)
    SetupGenerator().createUserTask(processName, workerName)
  def createSignalEvent(processName: String, workerName: String)(using config: SetupConfig): Unit =
    print("Signal Event", config.projectName, processName, workerName)
    SetupGenerator().createSignalEvent(processName, workerName)
  def createMessageEvent(processName: String, workerName: String)(using config: SetupConfig): Unit =
    print("Message Event", config.projectName, processName, workerName)
    SetupGenerator().createMessageEvent(processName, workerName)
  def createTimerEvent(processName: String, workerName: String)(using config: SetupConfig): Unit =
    print("Timer Event", config.projectName, processName, workerName)
    SetupGenerator().createTimerEvent(processName, workerName)

  private def print(label: String, projectName: String, processName: String, workerName: String) =
    println(s"Create $label: $workerName in $projectName / process: $processName")

end DevHelper
