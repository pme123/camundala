package camundala.api

import os.read.lines
import os.{Path, read}

import java.io.StringReader
import scala.annotation.tailrec
import scala.xml.*

/** Checks all BPMNs if a process is used in another process. As result a list
  * is created that can be included in the Documentation.
  */
trait ProcessReferenceCreator:

  protected def projectName: String
  protected def apiConfig: ApiConfig
  protected def gitBasePath: os.Path =     apiConfig.gitConfigs.gitDir 

  private def docProjectUrl(project: String): String =
    apiConfig.docProjectUrl(project)

  private lazy val projectPaths: Seq[ProjectConfig] =
    apiConfig.gitConfigs.projectConfigs

  lazy val allBpmns: Seq[(String, Seq[(Path, String)])] =
    projectPaths
      .map { case pc@ProjectConfig(name, path, bpmnPath, _, _) =>
        val absBpmnPath = pc.absBpmnPath(gitBasePath)
        name ->
          (if (os.exists(absBpmnPath)) os.walk(absBpmnPath)
           else {
             println(s"THIS PATH DOES NOT EXIST: $projectName")
             Seq.empty
           })
      }
      .map { case projectName -> path =>
        println(s"Get BPMNs in $projectName")
        projectName -> path
          .filterNot(_.toString.contains("/target"))
          .filter(_.toString.endsWith(".bpmn"))
          .map(p => {
            println(s"- ${p.last}")
            p -> read(p)
          })
      }

  case class UsedByReferenceCreator(refId: String):

    def create(): String =
      val refs = findUsagesInBpmn()
      val refDoc = refs
        .map { case k -> processes =>
          s"""_${k}_
             |${processes.map(_._2).distinct.mkString("   - ", "\n   - ", "\n")}
             |""".stripMargin
        }
        .mkString("\n- ", "\n- ", "\n")
      if (refDoc.trim.length == 1)
        "\n**Used in no other Process.**\n"
      else
        s"""
           |<details>
           |<summary><b>${usedByTitle(refs.size)}</b></summary>
           |<p>
           |
           |$refDoc
           |
           |</p>
           |</details>
           |""".stripMargin

    private def findUsagesInBpmn(): Seq[(String, Seq[(String, String)])] =
      println(s"Find Used by References for $refId")
      allBpmns
        .flatMap { case (processName, paths) =>
          paths
            .filter { case _ -> c =>
              c.matches(s"""[\\s\\S]*(:|")$refId"[\\s\\S]*""") &&
                !c.contains(s"id=\"$refId\"")
            }
            .map(pc => docuPath(processName, pc._1, pc._2))
        }
        .groupBy(_._1)
        .toSeq
        .sortBy(_._1)

    private def docuPath(
        projectName: String,
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
            val idPattern(id) = l: @unchecked
            id
          }
          .getOrElse(s"Id not found in $path")

      val anchor = s"#operation/Process:%20$extractId"
      projectName -> s"[$extractId](${docProjectUrl(projectName)}/OpenApi.html$anchor)"

    private def usedByTitle(processCount: Int): String =
      s"Used in $processCount Project(s)"

  end UsedByReferenceCreator

  case class UsesReferenceCreator(processName: String):

    def create(): String =
      println(s"Uses for $processName")
      findBpmn(processName)
        .map { xmlStr =>
          val xml = XML.load(new StringReader(xmlStr))
          val callActivities = (xml \\ "callActivity")
            .map { ca =>
              val calledElement = ca \@ "calledElement"
              val maybeServiceName = (ca \\ "in")
                .filter(_ \@ "target" == "serviceName")
                .map(_ \@ "sourceExpression")
                .headOption
              UsesRef(calledElement, maybeServiceName)
            }
          val businessRuleTasks = (xml \\ "businessRuleTask")
            .map { br =>
              val decisionRef = br
                .attribute("http://camunda.org/schema/1.0/bpmn", "decisionRef")
                .get
              UsesRef(decisionRef.toString, isDmn = true)
            }

          val refs = (callActivities ++ businessRuleTasks)
            .groupBy(_.project)
            .toSeq
            .sortBy(_._1)

          val refDoc = refs
            .map { case k -> processes =>
              s"""_${k}_
                 |${processes
                .map(_.asString)
                .distinct
                .sorted
                .mkString("   - ", "\n   - ", "\n")}
                 |""".stripMargin
            }
            .mkString("\n- ", "\n- ", "\n")
          if (refDoc.trim.length == 1)
            "\n**Uses no other Processes.**\n"
          else
            s"""
               |<details>
               |<summary><b>${usesTitle(refs.size)}</b></summary>
               |<p>
               |
               |$refDoc
               |</p>
               |</details>
               |""".stripMargin
        }
        .getOrElse("\n**Uses no other Processes.**\n")

    class UsesRef(
        processRef: String,
        serviceName: Option[String] = None,
        isDmn: Boolean = false
    ):
      lazy val (project: String, processId: String) =
        processRef.split(":").toList match
          case proj :: proc :: _ => (proj, proc)
          case proc :: _ => (projectName, proc)
          case Nil =>
            throw new IllegalArgumentException(
              "There must be at least a processId defined."
            )

      lazy val processIdent: String = serviceName.getOrElse(processId)
      lazy val anchor =
        s"#operation/${if (isDmn) "DecisionDmn" else "Process"}:%20$processIdent"
      lazy val dmnTag = if (isDmn)
        println(s"processRef:: $processRef")
        "(DMN)"
      else ""
      lazy val serviceStr: String =
        serviceName.map(_ => s" ($processId)").getOrElse("")

      lazy val asString: String =
        s"_[$processIdent](${docProjectUrl(project)}/OpenApi.html$anchor)_ $serviceStr$dmnTag"

    end UsesRef

    private def findBpmn(
        processName: String
    ): Option[String] =
      println(s"Find own BPMN for $processName")
      allBpmns.flatMap { case _ -> paths =>
        paths
          .filter { case _ -> content =>
            content.contains(s"id=\"$processName\"")
          }
          .map(_._2)
      }.headOption

    private def usesTitle(processCount: Int): String =
      s"Uses $processCount Project(s)"

  end UsesReferenceCreator
