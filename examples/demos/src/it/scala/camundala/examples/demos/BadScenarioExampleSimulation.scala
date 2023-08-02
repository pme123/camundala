package camundala.examples.demos

import BadScenarioExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

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

end BadScenarioExampleSimulation
