package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

object SimulationTestOverridesExample extends BpmnDsl:

  case class SimpleObject(name: String = "salu", other: Boolean = false)

  object SimpleObject:
    given ApiSchema[SimpleObject] = deriveSchema
    given JsonCodec[SimpleObject] = deriveCodec

  case class InOutput(
                       simpleValue: String = "hello",
                       collectionValue: Seq[String] = Seq("hello", "bye"),
                       objectValue: SimpleObject = SimpleObject(),
                       objectCollectionValue: Seq[SimpleObject] = Seq(SimpleObject(), SimpleObject("tschau", true)),
                     )
  object InOutput:
    given ApiSchema[InOutput] = deriveSchema
    given JsonCodec[InOutput] = deriveCodec

  lazy val simulationProcess = process(
    id = "simulation-TestOverrides",
    in = InOutput(),
    out = InOutput()
  )

end SimulationTestOverridesExample
