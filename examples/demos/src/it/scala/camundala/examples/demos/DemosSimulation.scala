package camundala.examples.demos

import camundala.simulation.custom.CustomSimulation
import camundala.simulation.gatling.GatlingSimulation

trait DemosSimulation extends CustomSimulation :
  override implicit def config =
    super.config.withPort(8033)