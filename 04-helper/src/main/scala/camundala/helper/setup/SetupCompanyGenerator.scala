package camundala
package helper.setup

import camundala.helper.util.{RepoConfig, ReposConfig}

case class SetupCompanyGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    generateDirectories
    DirectoryGenerator().generate // generates myCompany-camundala project
    // needed helper classes
    CompanyWrapperGenerator().generate
    // scripts
    val scriptCreator = ScriptCreator()
    createOrUpdate(projects / "helperProject.sc", scriptCreator.projectCreate)
    // override createCompany
    createOrUpdate(os.pwd / "helperCompany.sc", scriptCreator.companyCreate)
  end generate

  private lazy val companyName = config.companyName

  private lazy val generateDirectories: Unit =
    os.makeDir.all(gitTemp)
    os.makeDir.all(docker)
    os.makeDir.all(docs)
    os.makeDir.all(companyCamundala)
    os.makeDir.all(projects)
  end generateDirectories

  private lazy val gitTemp = os.pwd / "git-temp"
  private lazy val docker = os.pwd / "docker"
  private lazy val docs = os.pwd / s"$companyName-docs"
  private lazy val companyCamundala = os.pwd / s"$companyName-camundala"
  private lazy val projects = os.pwd / "projects"

end SetupCompanyGenerator

object SetupCompanyGenerator:
  def init(companyName: String, repoConfig: RepoConfig.Artifactory) =
    createOrUpdate(os.pwd / api.defaultProjectPath, defaultProjectContent(companyName))
    SetupConfig.defaultConfig(s"$companyName-camundala")
      .copy(reposConfig =
        ReposConfig(
          repos = Seq(repoConfig),
          repoSearch = repoConfig.repoSearch,
          ammoniteRepos = Seq(repoConfig)
        )
      )
  end init

  private def defaultProjectContent(companyName: String) =
    s"""
       |org = "$companyName"
       |name = "$companyName-services"
       |version = "0.1.0-SNAPSHOT"
       |dependencies: {
       |  
       |}
       |""".stripMargin
end SetupCompanyGenerator
