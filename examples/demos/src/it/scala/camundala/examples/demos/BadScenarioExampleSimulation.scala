package camundala.examples.demos

import camundala.examples.demos.SignalMessageExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *BadScenarioExampleSimulation
class BadScenarioExampleSimulation extends DemosSimulation:

  simulate {
    badScenario(
      `Bad Scenario with Message`,
      500,
      Some("Unknown property used in expression: ${nonExistingVariable}")
    )
    badScenario(
      `Bad Scenario without Message`,
      500
    )
  }
  private lazy val `Bad Scenario with Message` =
    process(
      "badScenario-example"
    )
  private lazy val `Bad Scenario without Message` =
    `Bad Scenario with Message`

end BadScenarioExampleSimulation
