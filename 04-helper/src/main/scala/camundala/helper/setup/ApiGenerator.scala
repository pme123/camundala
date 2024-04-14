package camundala.helper.setup

case class ApiGenerator()(using config: SetupConfig):

  lazy val generate: Unit =
    createOrUpdate(
      config.projectDir / ModuleConfig.apiModule.packagePath(config.projectPath) / "ApiProjectCreator.scala",
      api)

  lazy val api =
    s"""package ${config.projectPackage}
       |package api
       |
       |import bpmn.*
       |
       |object ApiProjectCreator extends CompanyApiCreator:
       |
       |  lazy val projectName: String = "${config.projectName}"
       |
       |  val title = "${config.projectClassName}"
       |
       |  lazy val projectDescr =
       |    "TODO Your Project description."
       |
       |  val version = "0.1.0-SNAPSHOT"
       |
       |  document(
       |    myProcessApi,
       |    //..
       |  )
       |
       |  private lazy val myProcessApi =
       |    import myProcess.*
       |    api(MyProcess.example)
       |
       |end ApiProjectCreator
       |""".stripMargin
  end api

end ApiGenerator
