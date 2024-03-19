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
  
  def createCustomWorker(processName: String, workerName: String)(using config: SetupConfig): Unit =
    println(s"Create Custom Worker: $workerName in ${config.projectName} / process: $processName")
    SetupGenerator().createCustomWorker(processName, workerName)

end DevHelper
