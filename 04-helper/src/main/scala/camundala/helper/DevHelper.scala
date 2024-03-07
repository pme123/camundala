package camundala.helper

import camundala.helper.setup.*
import camundala.helper.util.RepoConfig

object DevHelper:

  // company (./helperCompany.sc)
  def createUpdateCompany(companyName: String, repoConfig: RepoConfig.Artifactory) =
    println(s"REPO: $repoConfig")
    given config: SetupConfig = SetupCompanyGenerator.init(companyName, repoConfig)
    println(s"REPO2: ${config.reposConfig}")
    SetupCompanyGenerator().generate
  end createUpdateCompany

  // project (./projects/helperProject.sc)
  def updateProject(config: HelperConfig): Unit =
    SetupGenerator()(using config.setupConfig).generate

  // project (./projects/mycompany-myproject/helper.sc)
  def update(config: HelperConfig, subProjects: Seq[String]): Unit =
    given SetupConfig = config.setupConfig
      .copy(subProjects = subProjects)
    SetupGenerator().generate

end DevHelper
