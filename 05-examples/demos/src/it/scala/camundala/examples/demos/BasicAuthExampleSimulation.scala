package camundala.examples.demos

import camundala.examples.demos.DateExample.*
import camundala.simulation.*
import camundala.simulation.custom.*

// exampleDemos/It/testOnly *BasicAuthExampleSimulation
class BasicAuthExampleSimulation extends BasicSimulationDsl:

  simulate (
    DateExampleDMN
  )

  override implicit def config =
    super.config.withPort(8033)