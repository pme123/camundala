package camundala.helper.dev.company

import camundala.helper.dev.update.createOrUpdate
import camundala.helper.util.*

case class CompanyWrapperGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createOrUpdate(projectDevPath, helperConfig)

  private lazy val companyName = config.companyName
  private lazy val helperPath =
    config.projectDir / ModuleConfig.helperModule.packagePath(config.projectPath)
  private lazy val projectDevPath = helperPath / "CompanyDevHelper.scala"

  private lazy val helperConfig =
    objectContent("CompanyDevHelper"):
      """
        |   def config(projectName: String): DevConfig =
        |     DevConfig.defaultConfig(projectName) //TODO Implement your Config!
        |""".stripMargin
  end helperConfig

  private def objectContent(objName: String)(body: String) =
    s"""package $companyName.helper
       |
       |import camundala.helper.dev.*
       |import camundala.helper.util.*
       |
       |object $objName:
       |
       |$body
       |end $objName""".stripMargin
end CompanyWrapperGenerator
