package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.*
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *TimerExampleSimulation
class TimerExampleSimulation extends DemosSimulation:

  simulate(
    scenario(`timerProcess waiting for job`)(
      TheTimer.example
    ),
    scenario(`timerProcess waiting for variable`)(
      TheTimer.example
        .waitFor("timerReady")
    )
  )

  private lazy val `timerProcess waiting for job`      = TimerExample.example
  private lazy val `timerProcess waiting for variable` = TimerExample.example
end TimerExampleSimulation
