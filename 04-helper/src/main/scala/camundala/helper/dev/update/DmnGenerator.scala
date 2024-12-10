package camundala.helper.dev.update

import camundala.api.docs.DependencyConf

case class DmnGenerator()(using config: DevConfig):

  lazy val generate: Unit =
    createIfNotExists(dmnPath() / "ProjectDmnTester.scala", dmnTester)

  lazy val dmnTester: String =
    s"""package ${config.projectPackage}.dmn
       |
       |import ${config.projectPackage}.bpmn.*
       |
       |// dmn/run
       |object ProjectDmnTester extends ValiantDmnTester:
       |
       |  createDmnConfigs(
       |    // myDmn
       |  )
       |  /* example:
       |  private lazy val myDmn =
       |    import myProcess.v1.*
       |
       |    MyDmn.example
       |      .testUnit
       |      .testValues(
       |        _.value,
       |        1,
       |        2
       |      )
       |      .testValues(
       |        _.age,
       |        64,
       |        65,
       |        66
       |      )
       |  */
       |
       |end ProjectDmnTester""".stripMargin
  end dmnTester

  private def dmnPath(setupElement: Option[SetupElement] = None) =
    val dir =
      config.projectDir / ModuleConfig.dmnModule.packagePath(config.projectPath)

    os.makeDir.all(dir)
    dir
  end dmnPath

end DmnGenerator
