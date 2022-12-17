package camundala.examples.demos

import camundala.examples.demos.DateExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/GatlingIt/testOnly *DateExampleSimulation
// exampleDemos/It/run *DateExampleSimulation
class DateExampleSimulation extends DemosSimulation:

  simulate {
    scenario(DateExampleDMN)

  }