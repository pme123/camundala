package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

import java.time.LocalDateTime

object SimulationTestOverridesExample extends BpmnDsl:

  case class SimpleObject(name: String = "salu", other: Boolean = false)

  object SimpleObject:
    given Schema[SimpleObject] = Schema.derived
    given Encoder[SimpleObject] = deriveEncoder
    given Decoder[SimpleObject] = deriveDecoder

  case class InOutput(
                       simpleValue: String = "hello",
                       collectionValue: Seq[String] = Seq("hello", "bye"),
                       objectValue: SimpleObject = SimpleObject(),
                       objectCollectionValue: Seq[SimpleObject] = Seq(SimpleObject(), SimpleObject("tschau", true)),
                     )
  object InOutput:
    given Schema[InOutput] = Schema.derived
    given Encoder[InOutput] = deriveEncoder
    given Decoder[InOutput] = deriveDecoder

  lazy val simulationProcess = process(
    id = "simulation-TestOverrides",
    in = InOutput(),
    out = InOutput()
  )

end SimulationTestOverridesExample