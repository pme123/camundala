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
       |@main
       |def run(command: String, arguments: String*): Unit =
       |  ${config.companyClassName}DevHelper.run(command, arguments*)
       |""".stripMargin
  end projectHelper

  private lazy val companyName = config.companyName
  private lazy val reposConfig = config.reposConfig
  private lazy val versionHelper = CompanyVersionHelper(companyName, reposConfig.repoSearch)
  private lazy val helperHeader =
    s"""#!/usr/bin/env -S scala shebang
       |// $doNotAdjust. This file is replaced by `./helper.scala update`.
       |
       |//> using toolkit 0.5.0
       |//> using dep $companyName::$companyName-camundala-helper:${versionHelper
        .companyCamundalaVersion}
       |
       |import $companyName.camundala.helper.*
       |import camundala.helper.*
       |""".stripMargin

end ScriptCreator
