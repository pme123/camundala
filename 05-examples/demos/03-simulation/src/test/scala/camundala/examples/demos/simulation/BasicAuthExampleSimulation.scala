package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.DateExample.DateExampleDMN
import camundala.simulation.*
import camundala.simulation.custom.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *BasicAuthExampleSimulation
class BasicAuthExampleSimulation extends BasicSimulationDsl:

  simulate(
    DateExampleDMN
  )

  override implicit def config =
    super.config.withPort(8033)
end BasicAuthExampleSimulation
