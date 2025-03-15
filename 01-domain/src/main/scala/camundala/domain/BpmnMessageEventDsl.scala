package camundala.domain

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnMessageEventDsl extends BpmnDsl:

  def messageName: String

  def messageEvent[
      Msg <: Product: InOutEncoder: InOutDecoder: Schema
  ](
      in: Msg = NoInput(),
      id: Option[String] = None
  ): MessageEvent[Msg] =
    MessageEvent(
      messageName,
      InOutDescr(
        id.getOrElse(messageName),
        in,
        NoOutput(),
        msgNameDescr(messageName)
      )
    )
end BpmnMessageEventDsl
