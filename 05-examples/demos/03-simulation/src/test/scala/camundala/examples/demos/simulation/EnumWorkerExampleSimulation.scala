package camundala.examples.demos.simulation

import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.bpmn.{ErrorCodes, InputParams}
import camundala.domain.MockedServiceResponse
import camundala.examples.demos.bpmn.*
import camundala.examples.demos.bpmn.EnumWorkerExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *EnumWorkerExampleSimulation
class EnumWorkerExampleSimulation extends CustomSimulation:

  simulate(
    serviceScenario(
      example
    ),
    serviceScenario(
      exampleB
    )
  )

  override implicit def config =
    super.config
      .withPort(8887)
      .withMaxCount(20)
    //  .withLogLevel(LogLevel.DEBUG)

end EnumWorkerExampleSimulation
