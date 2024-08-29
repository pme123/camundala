package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object GenericServiceExample extends BpmnProcessDsl:
  val processName = "genericServiceExample"
  val descr = ""
  val companyDescr = ""

  case class Input(serviceName: String = "myservice.api.v1.post") extends GenericServiceIn
  object Input:
    given ApiSchema[Input] = deriveApiSchema
    given InOutCodec[Input] = deriveInOutCodec

  type Output = NoOutput

  lazy val example = process(
    in = Input(),
    out = NoOutput()
  )
end GenericServiceExample
