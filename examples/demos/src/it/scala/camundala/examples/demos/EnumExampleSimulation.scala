package camundala.examples.demos

import camundala.examples.demos.EnumExample.enumExample
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *EnumExampleSimulation
class EnumExampleSimulation extends DemosSimulation:

  simulate(
    enumExample
  )
