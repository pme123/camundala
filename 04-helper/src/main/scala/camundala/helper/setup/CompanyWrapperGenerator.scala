package camundala.helper.setup

case class CompanyWrapperGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(projectDevPath, helperConfig)

  private lazy val companyName = config.companyName
  private lazy val helperPath =
    config.projectDir / ModuleConfig.helperModule.packagePath(config.projectPath)
  private lazy val projectDevPath = helperPath / "ProjectDevHelper.scala"

  private lazy val helperConfig =
    objectContent("ProjectDevHelper"):
      """
        |   def config(projectName: String): HelperConfig = HelperConfig(
        |     setupConfig = ProjectSetupConfig.config(projectName) //TODO Implement your Config!
        |   )
        |""".stripMargin
  end helperConfig

  private def objectContent(objName: String)(body: String) =
    s"""package $companyName.helper
       |
       |import camundala.helper.*
       |import camundala.helper.setup.*
       |
       |object $objName:
       |
       |$body
       |end $objName""".stripMargin
end CompanyWrapperGenerator
