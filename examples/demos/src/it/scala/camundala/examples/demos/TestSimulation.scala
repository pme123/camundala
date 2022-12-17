package camundala.examples.demos

import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import camundala.simulation.gatling.GatlingSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleDemos/It/testOnly *TestSimulation
class TestSimulation extends DemosSimulation:

  import TestDomain.*
  simulate {
    scenario(CamundalaGenerateTestP)
  }
