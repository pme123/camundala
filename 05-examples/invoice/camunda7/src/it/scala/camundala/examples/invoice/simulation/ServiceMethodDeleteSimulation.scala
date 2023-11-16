package camundala.examples.invoice.simulation

import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.bpmn.{ErrorCodes, InputParams}
import camundala.domain.MockedServiceResponse
import camundala.examples.invoice.ServiceMethodDeleteApi.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceC7/It/testOnly *ServiceMethodDeleteSimulation
class ServiceMethodDeleteSimulation extends CustomSimulation:

  simulate(
    serviceScenario(
      `Service Method Delete`,
    ),
  )

  override implicit def config =
    super.config
      .withPort(8034)
    //  .withLogLevel(LogLevel.DEBUG)

  private lazy val `Service Method Delete` = example

end ServiceMethodDeleteSimulation
