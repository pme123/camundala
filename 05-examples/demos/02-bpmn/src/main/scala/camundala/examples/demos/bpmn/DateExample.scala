package camundala.examples.demos.bpmn

import camundala.domain.*
import java.time.LocalDateTime

//TODO this test throws a java.net.ConnectException
object DateExample extends BpmnProcessDsl:

  val processName = "NOT USED"
  def descr = ""

  case class Input(inDate: LocalDateTime = LocalDateTime.parse("2013-12-12T12:12:12"))

  object Input:
    given ApiSchema[Input] = deriveApiSchema
    given InOutCodec[Input] = deriveCodec

  lazy val example = process(
    Input(),
    NoOutput()
  )
  lazy val DateExampleDMN = singleEntry(
    decisionDefinitionKey = "DateExample",
    in = Input(),
    out =
      "2012-12-12T12:12:12.000+0100" // Camunda returns String - LocalDateTime.parse("2012-12-12T12:12:12")
  )

end DateExample
