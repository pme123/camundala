package camundala.helper.openApi

import scala.jdk.CollectionConverters.*

case class SimulationGenerator()(using val config: OpenApiConfig, val apiDefinition: ApiDefinition)
    extends GeneratorHelper:

  lazy val generate: Unit =
    os.remove.all(simulationPath)
    os.makeDir.all(simulationPath)
    apiDefinition.bpmnClasses
      .map:
        generateSimulation
      .map:
        case name -> content =>
          os.write.over(simulationPath / s"${name}Simulation.scala", content)
  end generate

  protected lazy val simulationPath: os.Path = config.simulationPath(superClass.versionPackage)
  protected lazy val simulationPackage: String = config.simulationPackage(superClass.versionPackage)

  private def generateSimulation(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.name
    val niceName = bpmnServiceObject.niceName
    name ->
      s"""package $simulationPackage
         |
         |import $bpmnPackage.$name.*
         |
         |// simulation/test
         |// simulation/testOnly *$simulationPackage*
         |// simulation/testOnly *${name}Simulation
         |class ${name}Simulation 
         |  extends ${config.superSimulationClass}:
         |
         |  simulate(
         |    serviceScenario(`$niceName`)
         |  )
         |
         |  private lazy val `$niceName` = example
         |  
         |end ${name}Simulation
         |
         |""".stripMargin
  end generateSimulation

end SimulationGenerator
