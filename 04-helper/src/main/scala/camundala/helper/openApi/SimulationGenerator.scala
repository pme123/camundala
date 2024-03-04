package camundala.helper.openApi

import io.swagger.v3.oas.models.media.Schema

import scala.jdk.CollectionConverters.*

case class SimulationGenerator()(using config: OpenApiConfig, apiDefinition: ApiDefinition):

  lazy val generate: Unit =
    os.remove.all(config.simulationPath)
    os.makeDir.all(config.simulationPath)
    apiDefinition.bpmnClasses
      .map:
        generateSimulation
      .map:
        case name -> content =>
          os.write.over(config.simulationPath / s"${name}Simulation.scala", content)
  end generate

  private def generateSimulation(bpmnServiceObject: BpmnServiceObject) =
    val name = bpmnServiceObject.name 
    val niceName = bpmnServiceObject.niceName    
      
    name ->
      s"""package ${config.simulationPackage}
         |
         |import ${config.bpmnPackage}.$name.*
         |
         |// simulation/test
         |// simulation/testOnly *${config.bpmnPackage}*
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
