package camundala.helper.setup

case class SimulationGenerator()(using config: SetupConfig):

  def createProcess(processName: String): Unit =
    val name = processName.head.toUpper + processName.tail
    os.write.over(
      simulationTestPath(processName) / s"${name}Simulation.scala",
      process(processName, name)
    )
  end createProcess

  private def process(
                       processName: String,
                       name: String
                     ) =
    s"""package ${config.projectPackage}
       |package simulation
       |package $processName
       |
       |import bpmn.$processName.$name.*
       |
       |object ${name}Simulation extends CompanySimulation:
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

  private def simulationTestPath(processName: String) =
    val dir = config.projectDir / ModuleConfig.simulationModule.packagePath(
      config.projectPath,
      mainOrTest = "test",
    ) / processName
    os.makeDir.all(dir)
    dir
  end simulationTestPath

end SimulationGenerator
