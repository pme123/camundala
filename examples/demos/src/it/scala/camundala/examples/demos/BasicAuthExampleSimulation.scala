package camundala.examples.demos

import camundala.examples.demos.DateExample.*
import camundala.simulation.*
import camundala.simulation.custom.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *BasicAuthExampleSimulation
class BasicAuthExampleSimulation extends BasicSimulationDsl:

  simulate (
    DateExampleDMN
  )

  override implicit def config =
    super.config.withPort(8033)