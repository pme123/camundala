package camundala
package helper.setup

import camundala.helper.util.ReposConfig

case class SetupCompanyCreator(companyName: String):

  lazy val create: Unit =
    generateDirectories
    createOrUpdate(os.pwd / api.defaultProjectPath, defaultProjectContent)

    given SetupConfig = SetupConfig.defaultConfig(s"$companyName-camundala")
    SetupGenerator().generate // generates myCompany-camundala project
   // createOrUpdate(projects / "createCompany.sv", createCompanyHelper)

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

  private lazy val defaultProjectContent =
    s"""
      |org = "$companyName"
      |name = "$companyName-services"
      |version = "0.1.0-SNAPSHOT"
      |dependencies: {
      |  
      |}
      |""".stripMargin
  private lazy val createCompanyHelper = ???
  //  HelperCreator(companyName).companyHelper

end SetupCompanyCreator
