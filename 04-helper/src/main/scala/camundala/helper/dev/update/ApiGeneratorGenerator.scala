package camundala.helper.dev.update

import camundala.api.docs.DependencyConf
import camundala.helper.util.DevConfig

case class ApiGeneratorGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createIfNotExists(apiGeneratorPath, apiGenerator)

  private lazy val companyName = config.companyName
  private lazy val helperPath =
    config.projectDir / ModuleConfig.helperModule.packagePath(config.projectPath)
  private lazy val apiGeneratorPath = helperPath / "ProjectApiGenerator.scala"
  private lazy val apiGenerator =
    objectContent("ProjectApiGenerator")

  private def objectContent(
      objName: String
  ) =
    s"""package ${config.projectPackage}.helper
       |
       |import camundala.helper.openApi.*
       |
       |object $objName extends App:
       |
       |  OpenApiGenerator().generate
       |
       |  private given OpenApiConfig = OpenApiConfig(
       |    projectName = "${config.projectName}",
       |
       |  )
       |  private given ApiDefinition = OpenApiCreator().create
       |
       |end $objName""".stripMargin

end ApiGeneratorGenerator
