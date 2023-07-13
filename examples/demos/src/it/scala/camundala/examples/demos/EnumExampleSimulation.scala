package camundala.examples.demos

import camundala.examples.demos.EnumExample.{Input, Output, enumExample}
import camundala.simulation.*

// exampleDemos/It/testOnly *EnumExampleSimulation
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