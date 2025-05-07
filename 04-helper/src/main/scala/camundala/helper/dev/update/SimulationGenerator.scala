package camundala.helper.dev.update

case class SimulationGenerator()(using config: DevConfig):

  def createProcess(setupElement: SetupElement): Unit =
    os.write.over(
      simulationTestPath(setupElement) / s"${setupElement.bpmnName}Simulation.scala",
      process(setupElement)
    )
  end createProcess

  private def process(
      setupElement: SetupElement
  ) =
    val SetupElement(_, processName, name, version) = setupElement
    s"""package ${config.projectPackage}
       |package simulation
       |
       |import ${config.projectPackage}.domain.$processName${version.versionPackage}.$name.*
       |
       |// ./helper.scala deploy ${name}Simulation
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
       |  override def config =
       |    super.config
       |      //.withMaxCount(30)
       |      //.withLogLevel(LogLevel.DEBUG)
       |
       |  private lazy val `${config.projectShortClassName} $name` =
       |    example.mockServices
       |
       |end ${name}Simulation""".stripMargin
  end process

  private def simulationTestPath(setupElement: SetupElement) =
    val dir = config.projectDir / ModuleConfig.simulationModule.packagePath(
      config.projectPath,
      mainOrTest = "test"
    )
    os.makeDir.all(dir)
    dir
  end simulationTestPath

end SimulationGenerator
