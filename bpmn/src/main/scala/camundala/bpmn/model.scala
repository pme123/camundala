package camundala
package bpmn

import camundala.domain.*

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import scala.language.implicitConversions

case class InOutDescr[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    id: String,
    in: In = NoInput(),
    out: Out = NoOutput(),
    descr: Option[String] = None
)

trait Activity[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: InOut[In, Out, T]
] extends InOut[In, Out, T]

trait InOut[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema,
    T <: InOut[In, Out, T]
] extends ProcessElement:
  def inOutDescr: InOutDescr[In, Out]
  //def constructor: InOutDescr[In, Out] => T
  lazy val inOutClass: String = this.getClass.getName
  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] = inOutDescr.descr
  lazy val in: In = inOutDescr.in
  lazy val out: Out = inOutDescr.out
  lazy val camundaInMap: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(in)
  lazy val camundaOutMap: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(out)
  def camundaToCheckMap: Map[String, CamundaVariable] = camundaOutMap

  def withInOutDescr(inOutDescr: InOutDescr[In, Out]): T

  def withId(i: String): T =
    withInOutDescr(inOutDescr.copy(id = i))

  def withDescr(description: String): T =
    withInOutDescr(inOutDescr.copy(descr = Some(description)))

  def withIn(in: In): T =
    withInOutDescr(inOutDescr.copy(in = in))

  // this allows you to manipulate the existing in directly
  def withIn(inFunct: In => In): T =
    withInOutDescr(inOutDescr.copy(in = inFunct(in)))

  def withOut(out: Out): T =
    withInOutDescr(
      inOutDescr.copy(out = out)
    )

  // this allows you to manipulate the existing out directly
  def withOut(outFunct: Out => Out): T =
    withInOutDescr(inOutDescr.copy(out = outFunct(out)))

trait ProcessElement extends Product:
  def id: String
  def typeName: String = getClass.getSimpleName
  def label: String = typeName.head.toString.toLowerCase + typeName.tail
  def descr: Option[String]

trait ProcessNode extends ProcessElement

// def endpoint: api.ApiEndpoint[In, Out, T]

case class Process[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out],
    elements: Seq[ProcessNode | InOut[?, ?, ?]] = Seq.empty
) extends InOut[In, Out, Process[In, Out]]:

  lazy val processName = inOutDescr.id

  def inOuts: Seq[InOut[?, ?, ?]] = elements.collect {
    case io: InOut[?, ?, ?] => io
  }

  def withInOutDescr(descr: InOutDescr[In, Out]): Process[In, Out] =
    copy(inOutDescr = descr)

  def withElements(
      elements: (ProcessNode | InOut[?, ?, ?])*
  ): Process[In, Out] =
    this.copy(elements = elements)
end Process

case class UserTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode,
      Activity[In, Out, UserTask[In, Out]]:

  override lazy val camundaToCheckMap: Map[String, CamundaVariable] =
    camundaInMap

  def withInOutDescr(descr: InOutDescr[In, Out]): UserTask[In, Out] =
    copy(inOutDescr = descr)

object UserTask:

  def init(id: String): UserTask[NoInput, NoOutput] =
    UserTask(
      InOutDescr(id, NoInput(), NoOutput())
    )
end UserTask

sealed trait ReceiveEvent[
    In <: Product: Encoder: Decoder: Schema,
    T <: ReceiveEvent[In, T]
] extends ProcessNode,
      Activity[In, NoOutput, T]

case class MessageEvent[
    In <: Product: Encoder: Decoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ReceiveEvent[In, MessageEvent[In]]:

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): MessageEvent[In] =
    copy(inOutDescr = descr)

object MessageEvent:

  def init(id: String): MessageEvent[NoInput] =
    MessageEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )
end MessageEvent

case class SignalEvent[
    In <: Product: Encoder: Decoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ReceiveEvent[In, SignalEvent[In]]:

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): SignalEvent[In] =
    copy(inOutDescr = descr)

object SignalEvent:

  def init(id: String): SignalEvent[NoInput] =
    SignalEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )
end SignalEvent

case class TimerEvent(
    title: String,
    inOutDescr: InOutDescr[NoInput, NoOutput]
) extends ReceiveEvent[NoInput, TimerEvent]:

  def withInOutDescr(descr: InOutDescr[NoInput, NoOutput]): TimerEvent =
    copy(inOutDescr = descr)

object TimerEvent:

  def init(title: String): TimerEvent =
    TimerEvent(title, InOutDescr(title, NoInput(), NoOutput()))
end TimerEvent

def valueToJson(value: Any): Json =
  value match
    case v: Int =>
      Json.fromInt(v)
    case v: Long =>
      Json.fromLong(v)
    case v: Boolean =>
      Json.fromBoolean(v)
    case v: Float =>
      Json.fromFloat(v).getOrElse(Json.Null)
    case v: Double =>
      Json.fromDouble(v).getOrElse(Json.Null)
    case null =>
      Json.Null
    case ld: LocalDate =>
      Json.fromString(ld.toString)
    case ldt: LocalDateTime =>
      Json.fromString(ldt.toString)
    case zdt: ZonedDateTime =>
      Json.fromString(zdt.toString)
    case v =>
      Json.fromString(v.toString)
