package camundala
package camunda

import camundala.bpmn.*
import camundala.domain.*

import scala.language.implicitConversions

case class Bpmn(path: Path, processes: BpmnProcess*)

case class BpmnProcess(
    process: Process[?, ?],
    elements: Seq[BpmnInOut[?,?]] = Seq.empty
):
  lazy val id: String = process.id

  def withElements(
                    elements:(InOut[?,?,?] | BpmnInOut[?,?])*
                  ): BpmnProcess =

    this.copy(elements = elements.map{
      case inOut: InOut[?,?,?] => BpmnInOut(inOut.asInstanceOf[InOut[Product, Product, ?]])
      case bpmnInOut: BpmnInOut[?,?] => bpmnInOut
    })

object BpmnProcess:
  implicit def elem(inOut: InOut[?,?, ?]): BpmnInOut[?,?] = BpmnInOut(inOut)

case class BpmnInOut[
  In <: Product,
  Out <: Product,
](
    inOut: InOut[In,Out, ?],
    outMappers: Seq[PathMapper] = Seq.empty,
    inMappers: Seq[PathMapper] = Seq.empty
):
  lazy val id: String = inOut.id

  def withOutMapper(pathMapper: PathMapper): BpmnInOut[In, Out] =
    copy(outMappers = outMappers :+ pathMapper)

  def withInMapper(pathMapper: PathMapper): BpmnInOut[In, Out] =
    copy(inMappers = inMappers :+ pathMapper)

case class PathMapper(
    varName: String,
    varType: MapType,
    path: List[PathEntry]
):
  // flag if it can be matched with a simple Expression:
  // ${myObj.prop("myField").stringValue()}
  // this is possible if there are only PathElems in the path.
  lazy val isInOutMapper: Boolean =
    path.forall(_.isInstanceOf[PathEntry.PathElem])

  def printGroovy(): String =
    toMappingEntries
      .map(_.printGroovy())
      .mkString("\n.")

  def printExpression(): String =
    toMappingEntries
     .map(_.name) match
      case Nil => throwErr("The Path is empty - It must have more than one element!")
      case x::Nil => throwErr(s"The Path is '$x' - It must have more than one element!")
      case x::xs =>
        s"$${" + x + xs.mkString(".prop(\"", "\").prop(\"", s"\")${varType.expr}") + "}"


  def toMappingEntries: List[MappingEntry] =
    val head = path.headOption match
      case Some(PathEntry.PathElem(n)) =>
        MappingEntry.ValueElem(n)
      case other =>
        throwErr("The Path must start")

    path.tail.foldLeft(List[MappingEntry](head)) {
      case result -> PathEntry.PathElem(n) =>
        result :+ MappingEntry.ValueElem(n)
      case result -> PathEntry.OptionalPath =>
        result.init :+ MappingEntry.OptionalElem(result.last.name)
    }

enum PathEntry:
  case OptionalPath
  case PathElem(name: String)

enum MapType(val expr: String):
  case Boolean extends MapType(".boolValue()")
  case Int extends MapType(".numberValue()")
  case Long extends MapType(".numberValue()")
  case Double extends MapType(".numberValue()")
  case String extends MapType(".stringValue()")
  case Json  extends MapType("")

object MapType:
  def apply(className: String): MapType =
    className match {
      case n if n.contains("Boolean") => MapType.Boolean
      case n if n.contains("Int") => MapType.Int
      case n if n.contains("Long") => MapType.Long
      case n if n.contains("Float") => MapType.Double
      case n if n.contains("Double") => MapType.Double
      case n if n.contains("String") => MapType.String
      case n => MapType.Json

    }

sealed trait MappingEntry:
  def name: String
  def printGroovy(): String

object MappingEntry:
  case class OptionalElem(name: String) extends MappingEntry:
    def printGroovy(): String = s"$name?"
  case class SeqElem(name: String) extends MappingEntry:
    def printGroovy(): String = "SeqElem"
  case class ValueElem(name: String) extends MappingEntry:
    def printGroovy(): String = name
