package camundala.examples.demos.bpmn

import camundala.bpmn.*
import camundala.domain.*

object MessageForExample extends BpmnProcessDsl:
  val processName = "message-for-example"
  val descr = ""
  val companyDescr = ""
  lazy val messageExample = process(
    in = SignalMessageExampleIn(),
    out = SignalMessageExampleOut()
  )
  lazy val messageIntermediateExample = messageEvent(
    "intermediate-message-for-example",
    in = SignalMessageExampleIn()
  )
end MessageForExample

object SignalExample extends BpmnProcessDsl:
  val processName = "signal-example"
  val descr = ""
  val companyDescr = ""

  lazy val signalExample = process(
    in = SignalMessageExampleIn(),
    out = SignalMessageExampleOut(endStatus = EndStatus.signalReceived)
  )
  lazy val signalIntermediateExample = signalEvent(
    "intermediate-signal-for-example",
    in = SignalMessageExampleIn()
  )
end SignalExample

case class SignalMessageExampleIn(someValue: String = "hello")
object SignalMessageExampleIn:
  given ApiSchema[SignalMessageExampleIn] = deriveApiSchema
  given InOutCodec[SignalMessageExampleIn] = deriveCodec

case class SignalMessageExampleOut(
    someValue: String = "hello",
    endStatus: EndStatus = EndStatus.messageReceived
)
object SignalMessageExampleOut:
  given ApiSchema[SignalMessageExampleOut] = deriveApiSchema
  given InOutCodec[SignalMessageExampleOut] = deriveCodec

enum EndStatus:
  case messageReceived, signalReceived

object EndStatus:
  given ApiSchema[EndStatus] = deriveEnumApiSchema
  given InOutCodec[EndStatus] = deriveEnumInOutCodec
