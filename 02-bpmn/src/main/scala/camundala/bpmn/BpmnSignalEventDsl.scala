package camundala.bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnSignalEventDsl extends BpmnDsl:

  def messageName: String

  def signalEvent[
    Msg <: Product : InOutEncoder : InOutDecoder : Schema
  ](
     in: Msg = NoInput(),
   ): SignalEvent[Msg] =
    SignalEvent(
      messageName,
      InOutDescr(
        messageName,
        in,
        NoOutput(),
        msgNameDescr(messageName)
      )
    )
end BpmnSignalEventDsl
