package camundala.helper.setup

case class SimulationGenerator()(using config: SetupConfig):

  def createProcess(processName: String, version: Option[Int]): Unit =
    val name = processName.head.toUpper + processName.tail
    os.write.over(
      simulationTestPath(version) / s"${name}Simulation.scala",
      process(processName, name, version)
    )
  end createProcess

  private def process(
      processName: String,
      name: String,
      version: Option[Int]
  ) =
    s"""package ${config.projectPackage}
       |package simulation${version.versionPackage}
       |
       |import ${config.projectPackage}.bpmn.$processName${version.versionPackage}.$name.*
       |
       |// amm helper.sc deploy ${name}Simulation
       |// simulation/test
       |// simulation/testOnly *${name}Simulation
       |class ${name}Simulation extends CompanySimulation:
       |
       |  simulate(
       |    scenario(`${config.projectShortClassName} $name`)(
       |      //TODO remove or add process steps like UserTasks
       |    )
       |  )
       |
       |  override implicit def config =
       |    super.config
       |      //.withMaxCount(30)
       |      //.withLogLevel(LogLevel.DEBUG)
       |
       |  private lazy val `${config.projectShortClassName} $name` =
       |    example.mockServices
       |
       |end ${name}Simulation""".stripMargin

  private def simulationTestPath(version: Option[Int]) =
    val dir = config.projectDir / ModuleConfig.simulationModule.packagePath(
      config.projectPath,
      mainOrTest = "test"
    )  / version.versionPath
    os.makeDir.all(dir)
    dir
  end simulationTestPath

end SimulationGenerator
