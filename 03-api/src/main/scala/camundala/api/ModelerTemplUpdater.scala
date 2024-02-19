package camundala.api

import camundala.api.*
import camundala.api.docs.ApiProjectConf
import io.circe.parser
import io.circe.syntax.*

import java.io.StringReader
import scala.language.postfixOps
import scala.xml.*
import scala.xml.transform.{RewriteRule, RuleTransformer}

case class ModelerTemplUpdater(apiConfig: ApiConfig):

  def update(): Unit =
    updateTemplates()
    updateBpmnColors()

  private def updateTemplates(): Unit =
    apiProjectConfig.dependencies
      .foreach: c =>
        val toPath = templConfig.templatePath / "dependencies"
        os.makeDir.all(toPath)
        val fromPath = projectsConfig.gitDir / c.name / templConfig.templateRelativePath
        println(s"Fetch dependencies: ${c.name} > $fromPath")
        if os.exists(fromPath) then
          os.walk(fromPath)
            .filter: p =>
              p.last.startsWith(c.name)
            .foreach: p =>
              parser.parse(os.read(p))
                .flatMap:
                  _.as[MTemplate]
                .map:
                  case t
                      if t.elementType.value == AppliesTo.`bpmn:CallActivity` &&
                        t.name != apiProjectConfig.name =>
                    val idWithPrefix = s"${apiConfig.projectRefId(t.id)._1}:${t.id}"
                    println(s" - Extend Template with prefix: $idWithPrefix")
                    val newTempl =
                      t.copy(properties =
                        t.properties
                          .map:
                            case p if p.value == t.id =>
                              p.copy(value = idWithPrefix)
                            case p => p
                      ).asJson
                        .deepDropNullValues
                        .toString
                    os.write.over(toPath / p.last, newTempl)

                  case t =>
                    println(s" - Just copy Template: ${t.id}")
                    os.copy(p, toPath / p.last, replaceExisting = true)
        else
          println(s"No Modeler Templates for $fromPath")
        end if
  end updateTemplates

  private def updateBpmnColors(): Unit =
    println("Adjust Color for:")
    projectsConfig.projectConfig(apiProjectConfig.name)
      .map: pc =>
        os.walk(os.pwd / pc.bpmnPath)
          .filter:
            _.toString.endsWith(".bpmn")
          .map: p =>
            p -> os.read(p)
          .map:
            extractUsesRefs
  end updateBpmnColors

  private lazy val templConfig = apiConfig.modelerTemplateConfig
  private lazy val projectsConfig = apiConfig.projectsConfig
  private lazy val apiProjectConfig = ApiProjectConf(apiConfig.projectConfPath)
  private lazy val colorMap = apiConfig.projectsConfig.colors.toMap

  private def extractUsesRefs(bpmnPath: os.Path, xmlStr: String) =
    println(s" - ${bpmnPath.last}")
    val xml: Node = XML.load(new StringReader(xmlStr))
    val callActivities = (xml \\ "callActivity")
      .map: ca =>
        val calledElement = ca \@ "calledElement"
        val id = ca \@ "id"
        apiConfig.projectRefId(calledElement)._1 -> id

    val externalWorkers = (xml \\ "serviceTask")
      .map: br =>
        val workerRef = br
          .attribute("http://camunda.org/schema/1.0/bpmn", "topic")
          .get
        val id = br \@ "id"
        apiConfig.projectRefId(workerRef.toString)._1 -> id

    val businessRuleTasks = (xml \\ "businessRuleTask")
      .map: br =>
        val decisionRef = br
          .attribute("http://camunda.org/schema/1.0/bpmn", "decisionRef")
          .get
        val id = br \@ "id"
        apiConfig.projectRefId(decisionRef.toString)._1 -> id

    val xmlNew = (callActivities ++ businessRuleTasks ++ externalWorkers)
      .filter:
        case project -> _ =>
          colorMap.contains(project) && apiProjectConfig.name != project
      .foldLeft(xml):
        case (xmlResult, project -> id) =>
          println(s"  -> $project > $id -- ${colorMap(project)}")
          new RuleTransformer(changeColor(project, id)).apply(xmlResult)
    os.write.over(bpmnPath, xmlNew.toString)
  end extractUsesRefs

  private def changeColor(project: String, id: String) = new RewriteRule:
    override def transform(n: Node): Seq[Node] = n match
      case e: Elem if e.label == "BPMNShape" && (e \@ "bpmnElement").equals(id) =>
        e % Attribute("color", "background-color", Text(s"${colorMap(project)}"), Null)
      case x => x

end ModelerTemplUpdater
