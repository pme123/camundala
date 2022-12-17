package camundala.examples.demos

import camundala.examples.demos.DateExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *DateExampleSimulation
class DateExampleSimulation extends DemosSimulation:

  simulate {
    scenario(DateExampleDMN)

  }