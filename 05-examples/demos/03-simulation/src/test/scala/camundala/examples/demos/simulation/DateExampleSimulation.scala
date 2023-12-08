package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.DateExample.DateExampleDMN
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *DateExampleSimulation
class DateExampleSimulation extends DemosSimulation:

  simulate(
    DateExampleDMN
  )

end DateExampleSimulation
