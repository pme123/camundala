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
  def update(config: HelperConfig, subProjects: Seq[String]): Unit =
    given SetupConfig = config.setupConfig
      .copy(subProjects = subProjects)
    println(s"Update Project: ${config.setupConfig.projectName}")
    println(s" - SubProjects: $subProjects")
    SetupGenerator().generate

end DevHelper
