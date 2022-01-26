package camundala
package api

import domain.*
import bpmn.*
import laika.api.*
import laika.ast.MessageFilter
import laika.format.*
import laika.markdown.github.GitHubFlavor
import sttp.tapir.docs.openapi.{OpenAPIDocsInterpreter, OpenAPIDocsOptions}
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.openapi.{Contact, Info, OpenAPI, Server}

import scala.reflect.ClassTag

trait APICreator extends App:

  def basePath: Path = pwd
  def openApiPath: Path = basePath / "openApi.yml"
  def openApiDocuPath: Path = basePath / "OpenApi.html"
  def postmanOpenApiPath: Path = basePath / "postmanOpenApi.yml"
  implicit def tenantId: Option[String] = None

  def title: String
  def serverPort = 8080
  def contact: Option[Contact] = None

  def createChangeLog(): String =
    val changeLogFile = basePath / "CHANGELOG.md"
    if (changeLogFile.toIO.exists())
      createChangeLog(read(changeLogFile))
    else
      "There is no CHANGELOG.md in the Package."

  def createReadme(): String =
    val readme = basePath / "README.md"
    if (readme.toIO.exists())
      read.lines(readme).tail.mkString("\n")
    else
      "There is no README.md in the Project."

  def description: Option[String] = Some(
    s"""
       |
       |Generated Open API:
       |
       |- **openApi.yml**: Documentation of the Processes (this Documentation😊).
       |- **postmanOpenApi.yml**: Open Api that you can import to Postman!
       |  Be aware that this needs some adjustments.
       |
       |>WARNING: This is an experimental way and not approved.
       |
       |${createReadme()}
       |
       |${createChangeLog()}
       |""".stripMargin
  )

  def version: String

  def servers = List(Server(s"http://localhost:$serverPort/engine-rest"))

  def info(title: String) = Info(title, version, description, contact = contact)

  def apiEndpoints[
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](processes: Process[In, Out]*): Unit =
    val endpoints = processes.map(_.endpoints())
    apiEndpoints(endpoints: _*)

  def apiEndpoints(apiEP: (Seq[ApiEndpoints] | ApiEndpoints)*): Unit =
    val ep: Seq[ApiEndpoints] = apiEP.flatMap {
      case s: Seq[?] => s.asInstanceOf[Seq[ApiEndpoints]]
      case s: ApiEndpoints => Seq(s)
    }
    writeOpenApi(openApiPath, openApi(ep))
 //   writeOpenApi(postmanOpenApiPath, postmanOpenApi(ep))
    println(s"Check Open API Docu: $openApiDocuPath")


  def openApi(apiEP: Seq[ApiEndpoints]): OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEP.flatMap(_.create()), info(title))
/*
  def postmanOpenApi(apiEP: Seq[ApiEndpoints]): OpenAPI =
    openAPIDocsInterpreter
      .toOpenAPI(apiEP.flatMap(_.createPostman()), info(title))
      .servers(servers)
*/
  lazy val openAPIDocsInterpreter = OpenAPIDocsInterpreter(docsOptions =
    OpenAPIDocsOptions.default.copy(defaultDecodeFailureOutput = _ => None)
  )

  def writeOpenApi(path: Path, api: OpenAPI): Unit =
    if (os.exists(path))
      os.remove(path)
    val yaml = api.toYaml
    os.write(path, yaml)
    println(s"Created Open API $path")

  private def createChangeLog(changeLog: String): String =

    val transformer = Transformer
      .from(Markdown)
      .to(HTML)
      .using(GitHubFlavor)
      .withRawContent
      .strict
      .failOnMessages(MessageFilter.None)
      // .renderMessages(MessageFilter.Error)
      .build
    transformer.transform(changeLog) match
      case Right(value) => s"""
                              |<details>
                              |<summary><b>CHANGELOG</b></summary>
                              |<p>
                              |
                              |$value
                              |
                              |</p>
                              |</details>
                              |""".stripMargin
      case Left(value) =>
        println(s"Problem CHANGELOG: $value")
        s"""CHANGELOG.md could not be created!
           |
           |Use Format:
           |
           |## 0.19.0 - 2021-09-10
           |### Fixed
           |- Fixed missing git push for master branch.
           |
           |## 0.18.1 - 2021-09-08
           |### Fixed
           |- Problem with Status 200 was not handled properly.
           |
           |### Added
           |- Helper to create ProceedProcess object.
           |
           |Problem:
           |
           |$value""".stripMargin

  extension [
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](processes: Map[String, Process[In, Out]])

    // override the processName
    def endpoint: ApiEndpoints =
      endpoints()

    // override the processName / tag
    def endpoint(tag: String, processName: String): ApiEndpoints =
      endpoints(Nil, Some(tag), Some(processName))

    def endpoints(activities: ApiEndpoint[_, _, _]*): ApiEndpoints =
      endpoints(activities, None, None)

    def endpoints(
        activities: Seq[ApiEndpoint[_, _, _]],
        tag: Option[String] = None,
        processName: Option[String] = None
    ): ApiEndpoints =
      val (name, process) = processes.headOption.getOrElse(
        throwErr("processes must have at least one entry.")
      )
      val inputExamples = processes.map { case (k, v) =>
        k -> v.in
      }
      val outputExamples = processes.map { case (k, v) =>
        k -> v.out
      }
      ApiEndpoints(
        tag
          .orElse(processName)
          .getOrElse(process.id),
        StartProcessInstance(
          processName
            .getOrElse(process.id),
          CamundaRestApi(
            process.id,
            tag.getOrElse(process.id),
            process.descr,
            RequestInput(inputExamples),
            RequestOutput.ok(outputExamples),
            startProcessInstanceErrors
          )
        ) +: activities
      )
  extension [
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](process: Process[In, Out])

    // override the processName / tag
    def endpoint(tag: String, processName: String): ApiEndpoints =
      endpoints(Nil, tag, processName)

    def endpoint: ApiEndpoints =
      endpoints()

    def endpoints(activities: ApiEndpoint[_, _, _]*): ApiEndpoints =
      endpoints(activities, process.id, process.id)

    def endpoints(
        activities: Seq[ApiEndpoint[_, _, _]],
        tag: String,
        processName: String
    ): ApiEndpoints =
      ApiEndpoints(
        tag,
        StartProcessInstance(
          processName,
          CamundaRestApi(
            process.inOutDescr,
            tag,
            requestErrorOutputs = startProcessInstanceErrors
          )
        ) +: activities
      )
  end extension

  extension [
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](userTask: UserTask[In, Out])
    def endpoint: ApiEndpoint[In, Out, UserTaskEndpoint[In, Out]] =
      UserTaskEndpoint(
        CamundaRestApi(
          userTask.inOutDescr,
          userTask.id,
          Nil
        ),
        GetActiveTask(
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.maybeDescr,
            requestErrorOutputs = getActiveTaskErrors
          )
        ),
        GetTaskFormVariables[In](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.maybeDescr,
            requestErrorOutputs = getTaskFormVariablesErrors
          )
        ),
        CompleteTask[Out](
          CamundaRestApi(
            userTask.id,
            userTask.id,
            userTask.maybeDescr,
            requestErrorOutputs = completeTaskErrors
          )
        )
      )
  end extension

  extension [
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](dmn: DecisionDmn[In, Out])
    def endpoint: ApiEndpoint[In, Out, EvaluateDecision[In, Out]] =
      EvaluateDecision(
        dmn,
        CamundaRestApi(
          dmn.inOutDescr,
          dmn.id,
          evaluateDecisionErrors
        )
      )
  end extension

end APICreator
