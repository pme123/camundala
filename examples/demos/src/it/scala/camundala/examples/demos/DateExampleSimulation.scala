package camundala.examples.demos

import camundala.examples.demos.DateExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/GatlingIt/testOnly *DateExampleSimulation
// exampleDemos/It/run *DateExampleSimulation
class DateExampleSimulation extends CustomSimulation:

  lazy val simulation = simulate {
    scenario(DateExampleDMN)

  }

  override implicit def config =
    super.config.withPort(8033)