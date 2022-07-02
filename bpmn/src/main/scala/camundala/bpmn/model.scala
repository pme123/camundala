package camundala
package bpmn

import java.util.Base64

case class InOutDescr[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    id: String,
    in: In = NoInput(),
    out: Out = NoOutput(),
    descr: Option[String] | String = None
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
  lazy val descr: Option[String] | String = inOutDescr.descr
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

  def withOut(out: Out): T =
    withInOutDescr(
      inOutDescr.copy(out = out)
    )
trait ProcessElement extends Product:
  def id: String
  def typeName: String = getClass.getSimpleName
  def label: String = typeName.head.toString.toLowerCase + typeName.tail
  def descr: Option[String] | String
  lazy val maybeDescr: Option[String] = descr match
    case d: Option[String] => d
    case d: String => Some(d)

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

  def asCallActivity: CallActivity[In, Out] =
    CallActivity(id, inOutDescr)

  def inOuts: Seq[InOut[?, ?, ?]] = elements.collect {
    case io: InOut[?, ?, ?] => io
  }

  def withInOutDescr(descr: InOutDescr[In, Out]): Process[In, Out] =
    copy(inOutDescr = descr)

  def withElements(
      elements: (ProcessNode | InOut[?, ?, ?])*
  ): Process[In, Out] =
    this.copy(elements = elements)

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

case class CallActivity[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    subProcessId: String,
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode,
      Activity[In, Out, CallActivity[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): CallActivity[In, Out] =
    copy(inOutDescr = descr)

  def asProcess: Process[In, Out] =
    Process(inOutDescr.copy(id = subProcessId))

object CallActivity:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): CallActivity[In, Out] =
    CallActivity(process.id, process.inOutDescr)

  def init(id: String): CallActivity[NoInput, NoOutput] =
    CallActivity(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )

case class ServiceTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode,
      Activity[In, Out, ServiceTask[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): ServiceTask[In, Out] =
    copy(inOutDescr = descr)

object ServiceTask:

  def init(id: String): ServiceTask[NoInput, NoOutput] =
    ServiceTask(
      InOutDescr(id, NoInput(), NoOutput())
    )

case class EndEvent(
    id: String,
    descr: Option[String] | String = None
) extends ProcessNode:

  def withDescr(descr: String): EndEvent =
    copy(descr = descr)

object EndEvent:

  def init(id: String): EndEvent =
    EndEvent(id)

case class ReceiveMessageEvent[
    In <: Product: Encoder: Decoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ProcessNode,
      Activity[In, NoOutput, ReceiveMessageEvent[In]]:

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): ReceiveMessageEvent[In] =
    copy(inOutDescr = descr)

object ReceiveMessageEvent:

  def init(id: String): ReceiveMessageEvent[NoInput] =
    ReceiveMessageEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )

case class ReceiveSignalEvent[
    In <: Product: Encoder: Decoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ProcessNode,
      Activity[In, NoOutput, ReceiveSignalEvent[In]]:

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): ReceiveSignalEvent[In] =
    copy(inOutDescr = descr)

object ReceiveSignalEvent:

  def init(id: String): ReceiveSignalEvent[NoInput] =
    ReceiveSignalEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )

case class NoInput()
object NoInput:
  given Schema[NoInput] = Schema.derived
  given Encoder[NoInput] = deriveEncoder
  given Decoder[NoInput] = deriveDecoder

case class NoOutput()
object NoOutput:
  given Schema[NoOutput] = Schema.derived
  given Encoder[NoOutput] = deriveEncoder
  given Decoder[NoOutput] = deriveDecoder

case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

object FileInOut:
  given Schema[FileInOut] = Schema.derived
  given Encoder[FileInOut] = deriveEncoder
  given Decoder[FileInOut] = deriveDecoder

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
    case v =>
      Json.fromString(v.toString)
