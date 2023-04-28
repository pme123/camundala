package camundala.examples.demos

import camundala.simulation.custom.CustomSimulation

trait DemosSimulation extends CustomSimulation :
  override implicit def config =
    super.config.withPort(8887)