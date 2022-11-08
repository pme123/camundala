package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*
import java.time.LocalDateTime

object DateExample extends BpmnDsl:

  case class Input(inDate: LocalDateTime = LocalDateTime.parse("2013-12-12T12:12:12"))

  lazy val DateExampleDMN = singleEntry(
    decisionDefinitionKey = "DateExample",
    in = Input(),
    out = LocalDateTime.parse("2012-12-12T12:12:12")
  )

 
  given Schema[Input] = Schema.derived
  given Encoder[Input] = deriveEncoder
  given Decoder[Input] = deriveDecoder


end DateExample
