package camundala.api

import os.read.lines
import os.{Path, read}

import scala.annotation.tailrec

/**
 * Checks all BPMNs if a process is used in another process.
 * As result a list is created that can be included in the Documentation.
 */
trait ProcessReferenceCreator:

  def docReference(processName: String): String =
    val refs = findBpmnFor(processName)
    s"""
       |<details>
       |<summary><b>${docTitle(refs.size)}</b></summary>
       |<p>
       |
       |${refs
      .map { case k -> processes =>
        s"""_${k}_
       |${processes.map(_._2).mkString("   - ", "\n   - ", "\n")}
       |""".stripMargin
      }
      .mkString("\n- ", "\n- ", "\n")}
       |
       |</p>
       |</details>
       |""".stripMargin

  protected def docTitle(processCount: Int): String =
    s"Used in the $processCount Process(es)"

  protected def docProjectUrl(project: String): String

  protected def projectPaths: Seq[Path] =
    Seq(
      os.pwd / os.up
    )

  protected def docuPath(
      projectPath: Path,
      path: Path,
      content: String
  ): (String, String) =
    val extractId =
      val pattern =
        """<(bpmn:process|process)([^\/>]+)isExecutable="true"([^\/>]*>)""".r
      val idPattern = """[\s\S]*id="([^"]*)"[\s\S]*""".r
      pattern
        .findFirstIn(content)
        .map { l =>
          val idPattern(id) = l
          id
        }
        .getOrElse(s"Id not found in $path")

    val extractId2 =
      val pattern = ".*id=\"([^\"]*)\".*".r
      lines(path).toList
        .dropWhile(!_.matches(".*<(bpmn:process|process).*"))
        .find(_.contains("id="))
        .map { l =>
          val pattern(id) = l
          id
        }
        .mkString

    @tailrec
    def projectName(segments: List[String]): (String, String) =
      segments match
        case projectName :: y :: _ if y == projectPath.last =>
          projectName -> s"[$extractId](${docProjectUrl(projectName)}/OpenApi.html#tag/$extractId)"
        case _ :: xs => projectName(xs)
        case Nil => "NOT_DEFINED" -> "projectName could not be extracted"

    projectName(path.segments.toList.reverse)

  protected lazy val allBpmns =
    projectPaths
      .map { p =>
        println(s"Get BPMNs in $p")
        p ->
          (if (os.exists(p)) os.walk(p)
          else {
            println(s"THIS PATH DOES NOT EXIST: $p")
            Seq.empty
          })
      }
      .map { case projectPath -> path =>
        projectPath -> path
          .filterNot(_.toString.contains("/target"))
          .filter(_.toString.endsWith(".bpmn"))
          .map(p => p -> read(p))
      }

  protected def findBpmnFor(
      processName: String
  ): Seq[(String, Seq[(String, String)])] =
    println(s"Find References for $processName")
    allBpmns
      .flatMap { case (pp, paths) =>
        paths
          .filter { case p -> c =>
            c.matches(s"""[\\s\\S]*(:|")$processName"[\\s\\S]*""") && !c.contains(s"id=\"$processName\"")
          }
          .map(pc => docuPath(pp, pc._1, pc._2))
      }
      .groupBy(_._1)
      .toSeq
      .sortBy(_._1)
