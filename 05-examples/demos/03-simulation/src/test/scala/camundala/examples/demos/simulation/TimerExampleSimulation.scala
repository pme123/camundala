package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.TimerExample.{timer, timerProcess}
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *TimerExampleSimulation
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