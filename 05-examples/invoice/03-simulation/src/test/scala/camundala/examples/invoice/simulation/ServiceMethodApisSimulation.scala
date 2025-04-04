package camundala.examples.invoice.simulation

import camundala.domain.GenericExternalTask.ProcessStatus
import camundala.domain.{ErrorCodes, InputParams, MockedServiceResponse}
import camundala.examples.invoice.bpmn.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceSimulation/test
// exampleInvoiceSimulation/testOnly *ServiceMethodApisSimulation
class ServiceMethodApisSimulation extends CustomSimulation:

  simulate(
    serviceScenario(
      `Service Method Delete`
    ),
    serviceScenario(
      `Service Method List`
    )
  )

  override implicit def config =
    super.config
      .withPort(8034)
    //  .withLogLevel(LogLevel.DEBUG)

  private lazy val `Service Method Delete` = ServiceMethodDeleteApi.example
  private lazy val `Service Method List` = ServiceMethodListApi.example

end ServiceMethodApisSimulation
