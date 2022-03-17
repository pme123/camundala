package camundala.examples.demos

import camundala.examples.demos.MappingDomain.CamundalaMappingExample
import camundala.gatling.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleDemos/GatlingIt/testOnly *MappingSimulation
class MappingSimulation extends SimulationRunner {

  override implicit def config: SimulationConfig =
    super.config.withPort(8033)


  import TestDomain.*
  simulate(
    processScenario(CamundalaMappingExample)()
  )
}
