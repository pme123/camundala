package camundala.examples.demos

import camundala.simulation.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*
import DateExample.*
import camundala.simulation.gatling.GatlingSimulation

import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *DateExampleSimulation
class DateExampleSimulation extends SimulationDsl, GatlingSimulation:

  override implicit def config =
    super.config.withPort(8033)

  simulate {
    scenario(DateExampleDMN)

  }

