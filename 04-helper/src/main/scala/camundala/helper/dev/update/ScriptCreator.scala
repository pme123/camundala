package camundala.helper.dev.update

import camundala.helper.util.CompanyVersionHelper

case class ScriptCreator()(using config: DevConfig):


  lazy val projectHelper =
    val projectName = config.projectName
    s"""$helperHeader
       |
       |lazy val projectName: String = "$projectName"
       |lazy val subProjects = Seq(
       |  ${config.subProjects.map(sp => s"\"$sp\"").mkString(", ")}
       |)
       |given util.DevConfig = CompanyDevHelper.config(projectName, subProjects)
       |
       |@main
       |def run(command: String, arguments: String*): Unit =
       |  dev.DevHelper.run(command, arguments*)
       |""".stripMargin
  end projectHelper

  private lazy val companyName = config.companyName
  private lazy val reposConfig = config.reposConfig
  private lazy val versionHelper = CompanyVersionHelper(companyName)
  private lazy val helperHeader =
    s"""#!/usr/bin/env -S scala shebang
       |// $doNotAdjust. This file is replaced by `./helper.scala update`.
       |
       |//> using toolkit 0.5.0
       |//> using dep $companyName::$companyName-camundala-helper:${versionHelper.companyCamundalaVersion}
       |
       |import $companyName.camundala.helper.*
       |import camundala.helper.*
       |""".stripMargin

end ScriptCreator
