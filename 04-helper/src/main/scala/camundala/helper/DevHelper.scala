package camundala.helper

import camundala.helper.setup.*
import camundala.helper.util.RepoConfig

object DevHelper:

  // company (.helperCompany.sc)
  def createUpdateCompany(companyName: String, repoConfig: RepoConfig.Artifactory) =
    println(s"REPO: $repoConfig")
    given config: SetupConfig = SetupCompanyGenerator.init(companyName, repoConfig)
    println(s"REPO2: ${config.reposConfig}")
    SetupCompanyGenerator().generate
  
  // project create (./projects/
  def updateProject(config: HelperConfig): Unit =
    SetupGenerator()(using config.setupConfig).generate  
  
end DevHelper
