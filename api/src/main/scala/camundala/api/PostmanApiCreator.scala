package camundala
package api

import ast.*
import bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.docs.openapi.*
import sttp.tapir.json.circe.*
import sttp.tapir.openapi.*
import sttp.tapir.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.EndpointIO.Example

import java.text.SimpleDateFormat
import java.util.Date
import scala.reflect.ClassTag
import scala.util.matching.Regex

trait PostmanApiCreator extends AbstractApiCreator:

  protected def createPostman(
      apiDoc: ApiDoc
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    println(s"Start Postman API: ${apiDoc.apis.size} top level APIs")
    apiDoc.apis.flatMap(_.createPostman())

  extension (groupedApi: GroupedApi)
    def createPostman(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      val apis = groupedApi.apis.flatMap(_.createPostman(groupedApi.name))
      groupedApi match
        case pa: ProcessApi[?, ?] =>
          createPostmanForProcess(pa, pa.name) ++ apis
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def createPostman(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case pa @ ProcessApi(name, inOut, _, apis) if apis.isEmpty =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          createPostmanForProcess(pa, tag)
        case ga: GroupedApi =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApis are allowed!\n$ga"
          )
        case aa @ ActivityApi(name, inOut, _) =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          inOut match
            case _: UserTask[?, ?] =>
              createPostmanForUserTask(aa, tag)
            case _: DecisionDmn[?, ?] =>
              createPostmanForDecisionDmn(aa, tag)
            case other =>
              println(s"TODO: $other")
              Seq.empty

  end extension

  protected def createPostmanForProcess(
      api: ProcessApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.startProcess(tag)
    )

  protected def createPostmanForUserTask(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.getActiveTask(tag),
      api.getTaskFormVariables(tag),
      api.completeTask(tag)
    )
  protected def createPostmanForDecisionDmn(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      api.evaluateDecision(tag)
    )

  extension (api: InOutApi[?, ?])
    private def postmanBaseEndpoint(
        tag: String,
        input: Option[EndpointInput[?]],
        label: String
    ): PublicEndpoint[?, Unit, Unit, Any] =
      Some(
        endpoint
          .tag(tag)
          .summary(s"${api.name}: $label")
          .description(api.descr)
      ).map(ep =>
        input
          .map(ep.in)
          .getOrElse(ep)
      ).get

  end extension

  extension (process: ProcessApi[?, ?])
    def startProcess(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path =
        tenantIdPath("process-definition" / "key" / process.inOut.id, "start")
      val input =
        process
          .toPostmanInput((example: FormVariables) =>
            StartProcessIn(
              example,
              Some(process.name)
            )
          )
      process
        .postmanBaseEndpoint(tag, input, "StartProcess")
        .in(path)
        .post

  end extension

  extension (api: ActivityApi[?, ?])

    def getActiveTask(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "task" / s"--REMOVE:${api.name}--"

      val input =
        api
          .toPostmanInput(_ => GetActiveTaskIn())
      api
        .postmanBaseEndpoint(tag, input, "GetActiveTask")
        .in(path)
        .post

    def getTaskFormVariables(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path =
        "task" / taskIdPath() / "form-variables" / s"--REMOVE:${api.name}--"

      api
        .postmanBaseEndpoint(tag, None, "GetTaskFormVariables")
        .in(path)
        .in(
          query[String]("variableNames")
            .description(
              """A comma-separated list of variable names. Allows restricting the list of requested variables to the variable names in the list.
                |It is best practice to restrict the list of variables to the variables actually required by the form in order to minimize fetching of data. If the query parameter is ommitted all variables are fetched.
                |If the query parameter contains non-existent variable names, the variable names are ignored.""".stripMargin
            )
            .default(
              api.apiExamples.inputExamples.defaultExample.example.productElementNames
                .mkString(",")
            )
        )
        .in(
          query[Boolean]("deserializeValues")
            .default(false)
        )
        .get

    def completeTask(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val path = "task" / taskIdPath() / "complete" / s"--REMOVE:${api.name}--"

      val input = api
        .toPostmanInput((example: FormVariables) => CompleteTaskIn(example),
          api.apiExamples.outputExamples.fetchExamples)

      api
        .postmanBaseEndpoint(tag, input, "CompleteTask")
        .in(path)
        .post

    def evaluateDecision(tag: String): PublicEndpoint[?, Unit, ?, Any] =
      val decisionDmn = api.inOut.asInstanceOf[DecisionDmn[?, ?]]
      val path = tenantIdPath(
        "decision-definition" / "key" / definitionKeyPath(
          decisionDmn.decisionDefinitionKey
        ),
        "evaluate"
      )
      val input = api
        .toPostmanInput((example: FormVariables) => EvaluateDecisionIn(example))

      api
        .postmanBaseEndpoint(tag, input, "EvaluateDecision")
        .in(path)
        .post

  end extension

  extension (inOutApi: InOutApi[?, ?])
    def toPostmanInput[
        T <: Product: Encoder: Decoder: Schema
    ](
        wrapper: FormVariables => T,
        examples: Seq[InOutExample[?]] =
        inOutApi.apiExamples.inputExamples.fetchExamples
    ): Option[EndpointInput[T]] =
      inOutApi.inOut.in match
        case _: NoInput =>
          None
        case _ =>
          Some(
            jsonBody[T]
              .examples(examples.map { case ex @ InOutExample(label, _) =>
                Example(
                  wrapper(ex.toCamunda),
                  Some(label),
                  None
                )
              }.toList)
          )
  end extension

  private def tenantIdPath(
      basePath: EndpointInput[?],
      pathElem: String
  ): EndpointInput[?] =
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / pathElem)
      .getOrElse(basePath / pathElem)

  private def tenantIdPath(id: String): EndpointInput[String] =
    path[String]("tenant-id")
      .description("The tenant, the process is deployed for.")
      .default(id)

  private def taskIdPath() =
    path[String]("taskId")
      .description("""The taskId of the Form.
                     |> This is the result id of the `GetActiveTask`
                     |
                     |Add in the _Tests_ panel of _Postman_:
                     |```
                     |let result = pm.response.json()[0];
                     |pm.collectionVariables.set("taskId", result.id)
                     |```
                     |""".stripMargin)
      .default("{{taskId}}")

  private def definitionKeyPath(key: String): EndpointInput[String] =
    path[String]("key")
      .description(
        "The Process- or Decision-DefinitionKey of the Process or Decision"
      )
      .default(key)

end PostmanApiCreator
