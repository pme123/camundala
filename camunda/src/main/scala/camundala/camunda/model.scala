package camundala
package camunda

import camundala.bpmn.*

case class Bpmn(path: Path, processes: Process[?, ?]*)

case class PathMapper(
                       varName: String,
                       varType: MapType,
                       path: Seq[PathEntry]
                     )

enum PathEntry:
  case OptionalPath
  case PathElem(name: String)

enum MapType:
  case Boolean, Int, Long, Double, String, Json

object MapType:
  def apply(className:String)  : MapType =
    className match {
      case n if n.contains("Boolean") => MapType.Boolean
      case n if n.contains("Int") => MapType.Int
      case n if n.contains("Long") => MapType.Long
      case n if n.contains("Float") => MapType.Double
      case n if n.contains("Double") => MapType.Double
      case n if n.contains("String") => MapType.String
      case n  => MapType.Json

    }
