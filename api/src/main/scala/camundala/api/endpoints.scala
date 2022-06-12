package camundala
package api

import camundala.bpmn.*
import camundala.bpmn.CamundaVariable.*
import io.circe.*
import io.circe.Json.JNumber
import io.circe.syntax.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.json.circe.*

import java.util.Base64
import scala.reflect.ClassTag

case class CamundaRestApi[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    name: String,
    tag: String,
    descr: Option[String] | String = None,
    requestInput: RequestInput[In] = RequestInput[In](),
    requestOutput: RequestOutput[Out] = RequestOutput[Out](),
    requestErrorOutputs: List[RequestErrorOutput] = Nil
):

  lazy val maybeDescr = descr match
    case d: Option[String] => d
    case d: String => Some(d)

  def outputErrors(): EndpointOutput[CamundaError] =
    requestErrorOutputs match
      case Nil =>
        Void()
      case x :: xs =>
        oneOf[CamundaError](
          errMapper(x),
          xs.map(output => errMapper(output)): _*
        )

  private def errMapper(
      output: RequestErrorOutput
  ): EndpointOutput.OneOfVariant[CamundaError] =
    oneOfVariant(
      output.statusCode,
      jsonBody[CamundaError].examples(output.examples.map { case (name, ex) =>
        Example(ex, Some(name), None)
      }.toList)
    )

  def inMapper[T <: Product: Encoder: Decoder: Schema](
      createInput: (example: In) => T
  ): Option[EndpointInput[T]] =
    if (requestInput.noInput)
      None
    else
      Some(
        jsonBody[T]
          .examples(requestInput.examples.map { case (label, ex) =>
            Example(
              createInput(ex),
              Some(label),
              None
            )
          }.toList)
      )
  def inMapper(): Option[EndpointInput[In]] =
    inMapper(x => x)

  def inMapper[T <: Product: Encoder: Decoder: Schema](
      body: T
  ): Option[EndpointInput[T]] =
    inMapper(_ => body)

  lazy val noInputMapper: Option[EndpointInput[In]] =
    None

  def outMapper[
      T <: Product | CamundaVariable | Json | Map[String, CamundaVariable] |
        Seq[
          Product | CamundaVariable | Json | Map[String, CamundaVariable]
        ]: Encoder: Decoder: Schema: ClassTag
  ](
      createOutput: (example: Out) => T
  ): Option[EndpointOutput[T]] =
    if (requestOutput.noOutdput)
      None
    else
      Some(
        oneOf[T](
          oneOfVariant(
            requestOutput.statusCode,
            jsonBody[T]
              .examples(requestOutput.examples.map { case (name, ex: Out) =>
                Example(
                  createOutput(ex),
                  Some(name),
                  None
                )
              }.toList)
          )
        )
      )
  def outMapper[
      T <: Product | Json | Map[String, CamundaVariable] |
        Seq[
          Product | Json | Map[String, CamundaVariable]
        ]: Encoder: Decoder: Schema: ClassTag
  ](
      body: T
  ): Option[EndpointOutput[T]] =
    outMapper(_ => body)

  def outMapper(): Option[EndpointOutput[Out]] =
    outMapper(x => x)

  lazy val noOutputMapper: Option[EndpointOutput[Out]] =
    None

end CamundaRestApi

object CamundaRestApi:

  def apply[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](
      e: InOutDescr[In, Out],
      tag: String,
      requestErrorOutputs: List[RequestErrorOutput]
  ): CamundaRestApi[In, Out] =
    CamundaRestApi(
      e.id,
      tag,
      e.descr,
      RequestInput(Map("standard" -> e.in)),
      RequestOutput.ok(Map("standard" -> e.out)),
      requestErrorOutputs
    )
end CamundaRestApi

case class ApiEndpoints[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    tag: String,
    processEndpoint: ApiEndpoint[In, ?, Out, ?],
    activityEndpoints: Seq[ApiEndpoint[?, ?, ?, ?]]
):
  lazy val endpoints: Seq[ApiEndpoint[?, ?, ?, ?]] =
    processEndpoint +: activityEndpoints

  def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    println(s"Start API: $tag - ${endpoints.size} Endpoints")
    endpoints.flatMap(_.withTag(tag).create())

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    endpoints.flatMap(_.withTag(tag).createPostman())

  def withInExample(label: String, example: In): ApiEndpoints[In, Out] =
    copy(
      processEndpoint = processEndpoint
        .withInExample(label, example)
    )

  def withOutExample(label: String, example: Out): ApiEndpoints[In, Out] =
    copy(
      processEndpoint = processEndpoint
        .withOutExample(label, example)
    )

  inline def withInExample(inline example: In): ApiEndpoints[In, Out] =
    withInExample(nameOfVariable(example), example)

  inline def withOutExample(inline example: Out): ApiEndpoints[In, Out] =
    withOutExample(nameOfVariable(example), example)

end ApiEndpoints

trait ApiEndpoint[
    In <: Product: Encoder: Decoder: Schema,
    PIn <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema: ClassTag,
    T <: ApiEndpoint[In, PIn, Out, T]
] extends Product:
  def restApi: CamundaRestApi[In, Out]

  def endpointType: String
  def apiName: String
  lazy val postmanName: String =
    s"${restApi.name}: ${getClass.getSimpleName}"
  lazy val tag: String = restApi.tag
  def descr: String = restApi.maybeDescr.getOrElse("")
  lazy val inExample: In = restApi.requestInput.examples.values.head
  lazy val outExample: Out = restApi.requestOutput.examples.values.head
  def outStatusCode: StatusCode
  protected def inMapper(): Option[EndpointInput[In]] =
    restApi.inMapper()
  protected def outMapper(): Option[EndpointOutput[Out]] =
    restApi.outMapper()
  protected def inMapperPostman()(using
      tenantId: Option[String]
  ): Option[EndpointInput[PIn]]

  def withRestApi(restApi: CamundaRestApi[In, Out]): T

  def withName(n: String): T =
    withRestApi(restApi.copy(name = n))

  def withTag(t: String): T =
    withRestApi(restApi.copy(tag = t))

  def withDescr(description: String): T =
    withRestApi(restApi.copy(descr = Some(description)))

  def withInExample(label: String, example: In): T =
    withRestApi(
      restApi.copy(requestInput = restApi.requestInput :+ (label, example))
    )

  def withOutExample(label: String, example: Out): T =
    withRestApi(
      restApi.copy(requestOutput = restApi.requestOutput :+ (label, example))
    )

  inline def withInExample(inline example: In): T =
    withInExample(nameOfVariable(example), example)

  inline def withOutExample(inline example: Out): T =
    withOutExample(nameOfVariable(example), example)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]]

  def postmanBaseEndpoint(using
      tenantId: Option[String]
  ): PublicEndpoint[?, Unit, Unit, Any] =
    Some(
      endpoint
        .name(s"$endpointType: $apiName")
        .tag(tag)
        .summary(s"$endpointType: $apiName")
        .description(descr)
    ).map((ep: Endpoint[Unit, Unit, Unit, Unit, Any]) =>
      inMapperPostman()
        .map((ei: EndpointInput[PIn]) => ep.in(ei))
        .getOrElse(ep)
    ).get

  def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    Seq(
      endpoint
        .name(s"$endpointType: $apiName")
        .tag(tag)
        .in(endpointType / apiName)
        .summary(s"$endpointType: $apiName")
        .description(descr)
        .head
    ).map(ep => inMapper().map(ep.in).getOrElse(ep))
      .map(ep => outMapper().map(ep.out).getOrElse(ep))

  protected def tenantIdPath(id: String): EndpointInput[String] =
    path[String]("tenant-id")
      .description("The tenant, the process is deployed for.")
      .default(id)

  protected def definitionKeyPath(key: String): EndpointInput[String] =
    path[String]("key")
      .description(
        "The Process- or Decision-DefinitionKey of the Process or Decision"
      )
      .default(key)
end ApiEndpoint

case class StartProcessInstance[
    In <: Product: Encoder: Decoder: Schema: ClassTag,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    processDefinitionKey: String,
    restApi: CamundaRestApi[In, Out],
    usedInDescr: Option[String] = None,
    usesDescr: Option[String] = None,
) extends ApiEndpoint[In, StartProcessIn, Out, StartProcessInstance[In, Out]]:
  val endpointType = "Process"
  val apiName = processDefinitionKey

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): StartProcessInstance[In, Out] =
    copy(restApi = restApi)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    Seq(
      postmanBaseEndpoint
        .in(postPath(processDefinitionKey))
        .post
    )

  private def postPath(name: String)(using tenantId: Option[String]) =
    val basePath =
      "process-definition" / "key" / name
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
      .getOrElse(basePath / "start")

  protected def inMapperPostman()(using
      tenantId: Option[String]
  ): Option[EndpointInput[StartProcessIn]] =
    restApi.inMapper[StartProcessIn] { (example: In) =>
      StartProcessIn(
        CamundaVariable.toCamunda(example)
      )
    }

  override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
    usedInDescr.mkString +
    usesDescr.mkString
  /*+
    s"""
       |
       |Usage as _CallActivity_:
       |```
       |lazy val $valueName =
       |          callActivity("$processDefinitionKey") //TODO adjust to your CallActivity id!
       |            .calledElement("$processDefinitionKey")
       |            ${inSources}
       |            ${outSources}
       |```
       |""".stripMargin */

  private lazy val inSources =
    inExample match
      case NoInput() => ""
      case _ =>
        inExample.productElementNames.mkString(
          """.inSource("""",
          """")
            |            .inSource("""".stripMargin,
          """")"""
        )

  private lazy val outSources =
    outExample match
      case NoOutput() => ""
      case _ =>
        outExample.productElementNames.mkString(
          """.outSource("""",
          """")
            |            .outSource("""".stripMargin,
          """")"""
        )

end StartProcessInstance

object StartProcessInstance:

  def apply[
      In <: Product: Encoder: Decoder: Schema: ClassTag,
      Out <: Product: Encoder: Decoder: Schema: ClassTag
  ](e: InOutDescr[In, Out]): StartProcessInstance[In, Out] =
    StartProcessInstance[In, Out](
      e.id,
      CamundaRestApi(
        e,
        e.id,
        startProcessInstanceErrors
      )
    )

end StartProcessInstance

case class GetTaskFormVariables[
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    restApi: CamundaRestApi[NoInput, Out]
) extends ApiEndpoint[NoInput, NoInput, Out, GetTaskFormVariables[Out]]:

  val apiName = "no API!"
  val endpointType = "no API!"
  override val descr = s"""Retrieves the form variables for a task.
                          |The form variables take form data specified on the task into account.
                          |If form fields are defined, the variable types and default values of the form fields are taken into account.
                          |
                          |${restApi.maybeDescr.getOrElse("")}""".stripMargin

  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[NoInput, Out]
  ): GetTaskFormVariables[Out] =
    copy(restApi = restApi)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    Seq(
      postmanBaseEndpoint.get
        .in(getPath)
        .in(
          query[String]("variableNames")
            .description(
              """A comma-separated list of variable names. Allows restricting the list of requested variables to the variable names in the list.
                |It is best practice to restrict the list of variables to the variables actually required by the form in order to minimize fetching of data. If the query parameter is ommitted all variables are fetched.
                |If the query parameter contains non-existent variable names, the variable names are ignored.""".stripMargin
            )
            .default(outExample.productElementNames.mkString(","))
        )
        .in(
          query[Boolean]("deserializeValues")
            .default(false)
        )
    )

  private lazy val getPath =
    "task" / taskIdPath() / "form-variables" / s"--REMOVE:${restApi.name}--"

  override protected def inMapperPostman()(using
      tenantId: Option[String]
  ) =
    restApi.noInputMapper

end GetTaskFormVariables

case class CompleteTask[
    In <: Product: Encoder: Decoder: Schema
](
    restApi: CamundaRestApi[In, NoOutput]
) extends ApiEndpoint[In, CompleteTaskIn, NoOutput, CompleteTask[In]]:

  val outStatusCode = StatusCode.Ok
  val endpointType = "no API!"
  val apiName = "no API!"

  def withRestApi(
      restApi: CamundaRestApi[In, NoOutput]
  ): CompleteTask[In] =
    copy(restApi = restApi)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    Seq(
      postmanBaseEndpoint
        .in(postPath)
        .post
    )

  private lazy val postPath =
    "task" / taskIdPath() / "complete" / s"--REMOVE:${restApi.name}--"

  override protected def inMapperPostman()(using
      tenantId: Option[String]
  ) =
    restApi.inMapper[CompleteTaskIn] { (example: In) =>
      CompleteTaskIn(CamundaVariable.toCamunda(example))
    }

end CompleteTask

case class GetActiveTask(
    restApi: CamundaRestApi[NoInput, NoOutput]
) extends ApiEndpoint[NoInput, GetActiveTaskIn, NoOutput, GetActiveTask]:

  val endpointType = "no Api!"
  val apiName = "no API!"
  val outStatusCode = StatusCode.Ok

  def withRestApi(
      restApi: CamundaRestApi[NoInput, NoOutput]
  ): GetActiveTask = copy(restApi = restApi)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    Seq(
      postmanBaseEndpoint
        .in(postPath)
        .post
    )

  private lazy val postPath =
    "task" / s"--REMOVE:${restApi.name}--"

  override protected def inMapperPostman()(using
      tenantId: Option[String]
  ) =
    restApi.inMapper(GetActiveTaskIn())

end GetActiveTask

case class UserTaskEndpoint[
    In <: Product: Encoder: Decoder: Schema: ClassTag,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    restApi: CamundaRestApi[In, Out],
    getActiveTask: GetActiveTask,
    getTaskFormVariables: GetTaskFormVariables[In],
    completeTask: CompleteTask[Out]
) extends ApiEndpoint[In, NoInput, Out, UserTaskEndpoint[In, Out]]:
  val outStatusCode = StatusCode.Ok //not used
  val endpointType = "UserTask"
  val apiName = restApi.name

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): UserTaskEndpoint[In, Out] = copy(restApi = restApi)

  def createPostman()(using
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, Unit, Unit, Any]] =
    val in = completeTask.restApi.copy(
      requestInput = RequestInput(restApi.requestOutput.examples)
    )
    val out = getTaskFormVariables.restApi.copy(requestOutput =
      RequestOutput(outStatusCode, restApi.requestInput.examples)
    )
    getActiveTask
      .withTag(restApi.tag)
      .createPostman() ++
      getTaskFormVariables
        .withRestApi(out)
        .withTag(restApi.tag)
        .createPostman() ++
      completeTask
        .withRestApi(in)
        .withTag(restApi.tag)
        .createPostman()

  override protected def inMapperPostman()(using
      tenantId: Option[String]
  ) = ???

end UserTaskEndpoint

private def taskIdPath() =
  path[String]("taskId")
    .description("""The taskId of the Form.
                   |> This is the result id of the `GetActiveTask`
                   |""".stripMargin)
    .default("{{taskId}}")

lazy val startProcessInstanceErrors = List(
  badRequest(
    s"""The instance could not be created due to an invalid variable value,
       |for example if the value could not be parsed to an Integer value or the passed variable type is not supported.
       |$errorHandlingLink""".stripMargin
  ),
  notFound(
    s"""The instance could not be created due to a non existing process definition key.
       |$errorHandlingLink""".stripMargin
  ),
  serverError(s"""The instance could not be created successfully.
                 |$errorHandlingLink""".stripMargin)
)

lazy val evaluateDecisionErrors = List(
  forbidden(
    s"""The authenticated user is unauthorized to evaluate this decision.
       |$errorHandlingLink""".stripMargin
  ),
  notFound(
    s"""The decision could not be evaluated due to a nonexistent decision definition.
       |$errorHandlingLink""".stripMargin
  ),
  serverError(s"""The decision could not be evaluated successfully,
                 | e.g. some of the input values are not provided but they are required.
                 |$errorHandlingLink""".stripMargin)
)

lazy val getActiveTaskErrors = List(
  badRequest(
    s"""Returned if some of the query parameters are invalid, for example if a sortOrder parameter is supplied, but no sortBy,
       | or if an invalid operator for variable comparison is used.
       |$errorHandlingLink""".stripMargin
  )
)

lazy val getTaskFormVariablesErrors = List(
  notFound(s"""Task id is null or does not exist.
              |$errorHandlingLink""".stripMargin)
)

lazy val completeTaskErrors = List(
  badRequest(
    s"""The variable value or type is invalid, for example if the value could not be parsed to an Integer value or the passed variable type is not supported.
       |$errorHandlingLink""".stripMargin
  ),
  serverError(
    s"""If the task does not exist or the corresponding process instance could not be resumed successfully.
       |$errorHandlingLink""".stripMargin
  )
)

def badRequest(msg: String = "Bad Request"): RequestErrorOutput =
  error(StatusCode.BadRequest).example(
    CamundaError("BadRequest", msg)
  )

def notFound(msg: String = "Not Found"): RequestErrorOutput =
  error(StatusCode.NotFound).example(
    CamundaError("NotFound", msg)
  )

def forbidden(msg: String = "Forbidden"): RequestErrorOutput =
  error(StatusCode.Forbidden).example(
    CamundaError("Forbidden", msg)
  )

def serverError(msg: String = "Internal Server Error"): RequestErrorOutput =
  error(StatusCode.InternalServerError).example(
    CamundaError("InternalServerError", msg)
  )

def error(statusCode: StatusCode): RequestErrorOutput =
  RequestErrorOutput(statusCode)

extension (request: RequestErrorOutput)
  def defaultExample: RequestErrorOutput =
    request.copy(examples = Map("defaultError" -> CamundaError()))

  def example(ex: CamundaError): RequestErrorOutput =
    request.copy(examples = request.examples + ("standardExample" -> ex))

  def example(
      `type`: String = "SomeExceptionClass",
      message: String = "a detailed message"
  ): RequestErrorOutput =
    request.copy(examples =
      request.examples + (`type` -> CamundaError(`type`, message))
    )
end extension

private val errorHandlingLink =
  s"See the [Introduction](https://docs.camunda.org/manual/$camundaVersion/reference/rest/overview/#error-handling) for the error response format."
