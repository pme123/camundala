package camundala.examples.demos

import camundala.bpmn.*
import camundala.domain.*

object SignalMessageExample extends BpmnDsl:

  lazy val messageExample = process(
    "message-for-example",
    in = SignalMessageExampleIn(),
    out = SignalMessageExampleOut()
  )
  lazy val signalExample = process(
    "signal-example",
    in = SignalMessageExampleIn(),
      out = SignalMessageExampleOut(endStatus = EndStatus.signalReceived)
  )
  lazy val messageIntermediateExample = receiveMessageEvent(
    "intermediate-message-for-example",
    in = SignalMessageExampleIn(),
  )
  lazy val signalIntermediateExample = receiveSignalEvent(
    "intermediate-signal-for-example",
    in = SignalMessageExampleIn(),
  )

  case class SignalMessageExampleIn(someValue: String = "hello")
  object SignalMessageExampleIn:
    given Schema[SignalMessageExampleIn] = Schema.derived
    given CirceCodec[SignalMessageExampleIn] = deriveCodec

  case class SignalMessageExampleOut(
                                      someValue: String = "hello",
                                      endStatus: EndStatus = EndStatus.messageReceived
                                    )
  object SignalMessageExampleOut:
    given Schema[SignalMessageExampleOut] = Schema.derived
    given CirceCodec[SignalMessageExampleOut] = deriveCodec

  enum EndStatus derives ConfiguredEnumCodec :
    case messageReceived, signalReceived

  object EndStatus:
    given Schema[EndStatus] = Schema.derived


end SignalMessageExample
