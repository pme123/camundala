package camundala.examples.demos

import camundala.examples.demos.EnumExample.{Input, Output, enumExample}
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/It/testOnly *EnumExampleSimulation
class EnumExampleSimulation extends DemosSimulation:

  simulate(
    enumExample,
    ignore.scenario(enumExampleWithNone),
    enumExampleThatFails
  )

  private lazy val enumExampleWithNone =
    enumExample
      .withIn(Input.A(someValue = None))
      .withOut(Output.A(someOut = None))

  private lazy val enumExampleThatFails =
    enumExample
      .withIn(Input.A(someValue = Some("other")))
      .withOut(Output.A())