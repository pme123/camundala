package camundala.examples.demos

import camundala.examples.demos.TimerExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *TimerExampleSimulation
class TimerExampleSimulation extends DemosSimulation:

  simulate(
    scenario(`timerProcess waiting for job`)(
      timer
    ),
    scenario (`timerProcess waiting for variable`)(
      timer
        .waitFor("timerReady")
    )
  )

  private lazy val `timerProcess waiting for job` = timerProcess
  private lazy val `timerProcess waiting for variable` = timerProcess