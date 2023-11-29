package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*
import java.time.LocalDateTime

object DateExample extends BpmnDsl:

  case class Input(inDate: LocalDateTime = LocalDateTime.parse("2013-12-12T12:12:12"))

  lazy val DateExampleDMN = singleEntry(
    decisionDefinitionKey = "DateExample",
    in = Input(),
    out = "2012-12-12T12:12:12.000+0100" //Camunda returns String - LocalDateTime.parse("2012-12-12T12:12:12")
  )

 
  given ApiSchema[Input] = deriveSchema
  given InOutCodec[Input] = deriveCodec


end DateExample
