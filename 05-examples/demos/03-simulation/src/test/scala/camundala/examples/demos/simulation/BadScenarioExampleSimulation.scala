package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.BadScenarioExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *BadScenarioExampleSimulation
class BadScenarioExampleSimulation extends DemosSimulation:

  simulate(
    badScenario(
      `Bad Scenario with Message`,
      500,
      Some("Unknown property used in expression: ${nonExistingVariable}")
    ),
    badScenario(
      `Bad Scenario without Message 2`,
      500
    )
  )

  lazy val `Bad Scenario without Message 2` =
    `Bad Scenario with Message`

end BadScenarioExampleSimulation
