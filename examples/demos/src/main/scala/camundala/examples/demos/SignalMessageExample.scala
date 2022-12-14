package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

import java.time.LocalDateTime

object SignalMessageExample extends BpmnDsl:

  lazy val messageExample = process(
    "message-for-example",
    out = SignalMessageExampleOut()
  )
  lazy val signalExample = process(
    "signal-for-example",
    out = SignalMessageExampleOut(endStatus = EndStatus.signalReceived)
  )

  case class SignalMessageExampleOut(endStatus: EndStatus = EndStatus.messageReceived)
  object SignalMessageExampleOut:
    given Schema[SignalMessageExampleOut] = Schema.derived
    given Encoder[SignalMessageExampleOut] = deriveEncoder
    given Decoder[SignalMessageExampleOut] = deriveDecoder

  enum EndStatus derives Adt.PureEncoder, Adt.PureDecoder :
    case messageReceived, signalReceived

  object EndStatus:
    given Schema[EndStatus] = Schema.derived


end SignalMessageExample
