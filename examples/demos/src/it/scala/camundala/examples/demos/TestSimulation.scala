package camundala.examples.demos

import camundala.simulation.*
import camundala.simulation.gatling.GatlingSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *TestSimulation
class TestSimulation extends SimulationDsl, GatlingSimulation:

  override implicit def config =
    super.config.withPort(8033)

  import TestDomain.*
  simulate {
    scenario(CamundalaGenerateTestP)
  }
