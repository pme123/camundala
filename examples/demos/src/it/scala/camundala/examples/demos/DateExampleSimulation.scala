package camundala.examples.demos

import camundala.simulation.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import DateExample.*
import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *DateExampleSimulation
class DateExampleSimulation extends SimulationDsl:

  override implicit def config: SimulationConfig =
    super.config.withPort(8033)

  simulate {
    scenario(DateExampleDMN)

  }

