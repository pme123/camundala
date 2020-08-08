package camundala.tools

import camundala.dsl.BpmnProcess._
import camundala.dsl.{BpmnProcess, ProcessNode, _}

object GeneratesDsl {

  def generate(bpmns: Bpmn*): String = {
    val allProcesses = bpmns.flatMap(_.processes).distinct
    val allNodes = allProcesses
      .flatMap(_.allNodes)
      .foldLeft(emptyAllNodes) {
        case (result, (k, nodes)) =>
          result.updatedWith(k)(existing =>
            Some((existing.toSeq.flatten ++ nodes).distinct)
          )
      }
    generateBpmns(bpmns) +
      generateProcesses(allProcesses) +
      generateNodes(allNodes)
  }

  def generateBpmns(bpmns: Seq[Bpmn]): String = {
    s"""
         |object bpmns {
         |import processes._
         |${bpmns
         .map { bpmn =>
           s"""|   lazy val ${idAsVal(bpmn.id)} =
              |      bpmn(s"${bpmn.id}")
              |        .processes(
              |           ${bpmn.processes
                .map(p => s"${idAsVal(p.id)}")
                .mkString(",\n           ")}
              |        )
              |""".stripMargin
         }
         .mkString("\n")}}""".stripMargin
  }

  def generateProcesses(processes: Seq[BpmnProcess]): String =
    s"""
         |object processes {
         |   import ${allNodeKeys.map(k => s"$k._").mkString(", ")}
         |   ${processes
         .map { p =>
           s"""lazy val ${idAsVal(p.id)} =
             |   process("${p.id}")
             |      ${p.allNodes.map {
                case (_, Nil) => ""
                case (k, nodes) =>
                  s""".$k (
                 |       ${nodes.map(_.id).mkString(",\n       ")}
                 |   )""".stripMargin
              }.mkString}
             |   """.stripMargin
         }
         .mkString("\n")}
         |}""".stripMargin

  def generateNodes(processNodes: Map[NodeKey, Seq[ProcessNode]]): String =
    processNodes.map {
      case (k, nodes) =>
        s"""
         |object $k {
         |   ${nodes
             .map { n =>
               s"""lazy val ${idAsVal(n.id)} =
             |      ${k.name}("${n.id}")"""
             }
             .mkString("\n   ")}
         |} """.stripMargin
    }.mkString

  private def idAsVal(id: Identifier): String =
    id.value.split("""[_.-]""").toList match {
      case Nil         => ""
      case head :: Nil => head
      case head :: tail =>
        head +
          tail.map(str => str.head.toUpper +: str.drop(1)).mkString
    }

}
