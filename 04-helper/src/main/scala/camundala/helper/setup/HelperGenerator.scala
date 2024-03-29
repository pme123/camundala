package camundala.helper.setup

import camundala.api.docs.DependencyConf


case class HelperGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(apiGeneratorPath, apiGenerator)

  private lazy val companyName = config.companyName
  private lazy val helperPath =
    config.projectDir / ModuleConfig.helperModule.packagePath(config.projectPath)
  private lazy val apiGeneratorPath = helperPath / "ProjectApiGenerator.scala"
  private lazy val apiGenerator =
    objectContent("ProjectApiGenerator")

  private def objectContent(
                             objName: String
                           ) =
    s"""package ${config.projectPackage}.camundala.helper
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

end HelperGenerator
