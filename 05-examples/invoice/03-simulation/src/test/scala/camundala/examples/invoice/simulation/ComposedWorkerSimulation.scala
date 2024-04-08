package camundala.examples.invoice.simulation

import camundala.examples.invoice.bpmn.*
import camundala.examples.invoice.bpmn.ComposedWorkerExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceSimulation/test
// exampleInvoiceSimulation/testOnly *ComposedWorkerSimulation
class ComposedWorkerSimulation extends CustomSimulation:

  simulate(
    scenario(`Composed Worker`)
  )
  override implicit def config =
    super.config
      .withPort(8034)
  //  .withLogLevel(LogLevel.DEBUG)

  private lazy val `Composed Worker` = example
end ComposedWorkerSimulation
