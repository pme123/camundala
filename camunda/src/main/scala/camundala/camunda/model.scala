package camundala
package camunda

import camundala.bpmn.*

case class Bpmn(path: Path, processes: BpmnProcess*)

case class BpmnProcess(
    process: Process[?, ?],
    elements: Seq[BpmnInOut] = Seq.empty
):
  lazy val id: String = process.id

  def withElements(
      elements: BpmnInOut*
  ): BpmnProcess =
    this.copy(elements = elements)

case class BpmnInOut(
    inOut: InOut[?, ?, ?],
    outMappers: Seq[PathMapper] = Seq.empty,
    inMappers: Seq[PathMapper] = Seq.empty
):
  lazy val id: String = inOut.id

  def withOutMapper(pathMapper: PathMapper): BpmnInOut =
    copy(outMappers = outMappers :+ pathMapper)

  def withInMapper(pathMapper: PathMapper): BpmnInOut =
    copy(inMappers = inMappers :+ pathMapper)

case class PathMapper(
    varName: String,
    varType: MapType,
    path: Seq[PathEntry]
):
  def printGroovy(): String =
    toMappingEntries
      .map(_.printGroovy())
      .mkString("\n.")

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

enum MapType:
  case Boolean, Int, Long, Double, String, Json

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
