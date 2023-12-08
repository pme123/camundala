package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.EnumExample.{Input, Output, enumExample}
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *EnumExampleSimulation
class EnumExampleSimulation extends DemosSimulation:

  simulate(
    enumExample,
    scenario(enumExampleWithNone),
    enumExampleOther
  )

  private lazy val enumExampleWithNone =
    enumExample
      .withIn(Input.A(someValue = None))
      .withOut(Output.A(someOut = None))

  private lazy val enumExampleOther =
    enumExample
      .withIn(Input.A(someValue = Some("other")))
      .withOut(Output.A(someOut = Some("other")))