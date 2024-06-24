package camundala.examples.invoice.simulation

import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.bpmn.{ErrorCodes, InputParams}
import camundala.domain.MockedServiceResponse
import camundala.examples.invoice.bpmn.People
import camundala.examples.invoice.bpmn.StarWarsPeople.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleInvoiceSimulation/test
// exampleInvoiceSimulation/testOnly *StarWarsPeopleSimulation
class StarWarsPeopleSimulation extends CustomSimulation:

  simulate(
    scenario(
      `Star Wars Api People real`
    )
  )

  override implicit def config =
  super.config
    .withPort(8034)
  //  .withLogLevel(LogLevel.DEBUG)

  private lazy val `Star Wars Api People Detail` = example

  private lazy val `Star Wars Api People real` = example
    .withOut(Out.Success(Seq(People(
      name = "Darth Vader",
      height = "202",
      mass = "136",
      hair_color = "none",
      skin_color = "white",
      eye_color = "yellow"
    ))))

end StarWarsPeopleSimulation
