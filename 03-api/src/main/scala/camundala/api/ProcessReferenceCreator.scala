package camundala.api

import camundala.bpmn.InOutType

import java.io.StringReader
import scala.xml.XML

/** Checks all BPMNs if a process is used in another process. As result a list is created that can
  * be included in the Documentation.
  */
trait ProcessReferenceCreator:

  protected def projectName: String
  protected def apiConfig: ApiConfig

  protected def refIdentShort(refIdent: String): String =
    apiConfig.refIdentShort(refIdent)
  protected def refIdentShort(refIdent: String, projectName: String): String =
    apiConfig.refIdentShort(refIdent, projectName)

  protected def gitBasePath: os.Path = apiConfig.projectsConfig.gitDir
  private def docProjectUrl(project: String): String =
    apiConfig.docProjectUrl(project)

  private lazy val projectConfigs: Seq[ProjectConfig] =
    apiConfig.projectsConfig.projectConfigs

  lazy val allBpmns: Seq[(String, Seq[(os.Path, String)])] =
    println(s"BPMN Reference Base Directory: $gitBasePath")
    projectConfigs
      .map { pc =>
        val absBpmnPath = pc.absBpmnPath(gitBasePath)
        pc.name ->
          (if os.exists(absBpmnPath) then
             os.walk(absBpmnPath)
           else
             println(s"THIS PATH DOES NOT EXIST: $projectName")
             Seq.empty
          )

      }
      .map { case projectName -> path =>
        println(s"Get BPMNs in $projectName")
        projectName -> path
          .filterNot(_.toString.contains("/target"))
          .filter(_.toString.endsWith(".bpmn"))
          .map(p =>
            println(s"- ${p.last}")
            p -> os.read(p)
          )
      }
  end allBpmns

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
      if refDoc.trim.length == 1 then
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
      end if
    end create

    private def findUsagesInBpmn(): Seq[(String, Seq[(String, String)])] =
      println(s"Find Used by References for $refId")
      allBpmns
        .flatMap { case (processName, paths) =>
          paths
            .filter { case _ -> c =>
              c.matches(s"""[\\s\\S]*(:|")$refId"[\\s\\S]*""") &&
              !c.contains(s"id=\"$refId\"")
            }
            .map { pc =>
              println(s"- $processName ${pc._1}")
              docuPath(processName, pc._1, pc._2)
            }
        }
        .groupBy(_._1)
        .toSeq
        .sortBy(_._1)
    end findUsagesInBpmn

    private def docuPath(
        projectName: String,
        path: os.Path,
        content: String
    ): (String, String) =
      val extractId =
        val pattern = """<(bpmn:process|process)([^\/>]+)isExecutable="true"([^\/>]*>)""".r
        val idPattern = """[\s\S]*id="([^"]*)"[\s\S]*""".r
        pattern
          .findFirstIn(content)
          .map { l =>
            val idPattern(id) = l: @unchecked
            id
          }
          .getOrElse(s"Id not found in $path")
      end extractId

      val refId = refIdentShort(extractId, projectName)
      val anchor = s"#operation/${InOutType.Bpmn}:%20$refId"
      projectName -> s"[${InOutType.Bpmn}: $refId](${docProjectUrl(projectName)}/OpenApi.html$anchor)"
    end docuPath

    private def usedByTitle(processCount: Int): String =
      s"Used in $processCount Project(s)"

  end UsedByReferenceCreator

  case class UsesReferenceCreator(processName: String):

    def create(): String =
      println(s"Uses for $processName")
      findBpmn(processName)
        .map { xmlStr =>
          val refs = extractUsesRefs(xmlStr)
          val refDoc = refs
            .map { case k -> processes =>
              println(s"- $k:\n -- ${processes.map(_.asString).mkString("\n  -- ")}")
              s"""_${k}_
                 |${processes
                  .map(_.asString)
                  .distinct
                  .sorted
                  .mkString("   - ", "\n   - ", "\n")}
                 |""".stripMargin
            }
            .mkString("\n- ", "\n- ", "\n")
          if refDoc.trim.length == 1 then
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
          end if
        }
        .getOrElse("\n**Uses no other Processes.**\n")
    end create

    case class UsesRef(
        processRef: String,
        serviceName: Option[String] = None,
        refType: InOutType = InOutType.Bpmn
    ):

      lazy val (project: String, processId: String) =
        apiConfig.projectRefId(processRef)

      lazy val processIdent: String = serviceName.getOrElse(processId)
      lazy val identShort = refIdentShort(processIdent)
      lazy val anchor = s"#operation/$refType:%20$identShort"

      lazy val serviceStr: String =
        serviceName.map(_ => s" ($processId)").getOrElse("")

      lazy val asString: String =
        s"_[$refType: $identShort](${docProjectUrl(project)}/OpenApi.html$anchor)_ $serviceStr"
    end UsesRef
    
    private def extractUsesRefs(xmlStr: String) =
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
      val externalWorkers = (xml \\ "serviceTask")
        //  .filter(_ \@ "topic" nonEmpty)
        .map { br =>
          val workerRef = br
            .attribute("http://camunda.org/schema/1.0/bpmn", "topic")
            .get
          UsesRef(workerRef.toString, refType = InOutType.Worker)
        }.filterNot(_.processRef == processName) // filter InitWorker

      val businessRuleTasks = (xml \\ "businessRuleTask")
        .map { br =>
          val decisionRef = br
            .attribute("http://camunda.org/schema/1.0/bpmn", "decisionRef")
            .get
          UsesRef(decisionRef.toString, refType = InOutType.Dmn)
        }

      (callActivities ++ businessRuleTasks ++ externalWorkers)
        .groupBy(_.project)
        .toSeq
        .sortBy(_._1)
    end extractUsesRefs

    private def findBpmn(
        processName: String
    ): Option[String] =
      allBpmns.flatMap { case _ -> paths =>
        paths
          .filter { case _ -> content =>
            content.contains(s"id=\"$processName\"")
          }
          .map(_._2)
      }.headOption
    end findBpmn

    private def usesTitle(processCount: Int): String =
      s"Uses $processCount Project(s)"

  end UsesReferenceCreator
end ProcessReferenceCreator
