package camundala
package bpmn

import domain.*

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple}
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

case class Bpmn(path: Path, processes: Process[?, ?]*)

case class InOutDescr[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    id: String,
    in: In = NoInput(),
    out: Out = NoOutput(),
    descr: Option[String] | String = None
)

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
  def label: String = getClass.getSimpleName.head.toString.toLowerCase + getClass.getSimpleName.tail
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
    elements: Seq[ProcessNode | InOut[?,?,?]] = Seq.empty
) extends InOut[In, Out, Process[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): Process[In, Out] =
    copy(inOutDescr = descr)

case class UserTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode, InOut[In, Out, UserTask[In, Out]]:

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
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode, InOut[In, Out, CallActivity[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): CallActivity[In, Out] =
    copy(inOutDescr = descr)

object CallActivity:
  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): CallActivity[In, Out] =
    CallActivity(process.inOutDescr)

case class ServiceTask[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode, InOut[In, Out, ServiceTask[In, Out]]:

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