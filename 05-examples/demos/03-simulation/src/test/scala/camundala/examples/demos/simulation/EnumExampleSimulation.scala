package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.EnumExample.{Input, Output, example}
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *EnumExampleSimulation
class EnumExampleSimulation extends DemosSimulation:

  simulate(
    example,
    scenario(enumExampleWithNone),
    enumExampleOther
  )

  private lazy val enumExampleWithNone =
    example
      .withIn(Input.A(someValue = None))
      .withOut(Output.A(someOut = None))

  private lazy val enumExampleOther =
    example
      .withIn(Input.A(someValue = Some("other")))
      .withOut(Output.A(someOut = Some("other")))
end EnumExampleSimulation
