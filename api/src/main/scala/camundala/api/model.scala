package camundala
package api

import bpmn.*
import domain.*
import io.circe.syntax.*
import io.circe.*
import sttp.model.StatusCode
import sttp.tapir.{Schema, SchemaType}
import sttp.tapir.Schema.annotations.description

import scala.collection.immutable

type ExampleName = String
  
case class RequestErrorOutput(
    statusCode: StatusCode,
    examples: Map[ExampleName, CamundaError] = Map.empty
)
object RequestErrorOutput:
  given Schema[RequestErrorOutput] = Schema.derived
  given CirceCodec[RequestErrorOutput] = deriveCodec
end RequestErrorOutput

case class CamundaError(
    `type`: String = "SomeExceptionClass",
    message: String = "a detailed message"
)
object CamundaError:
  given CirceCodec[CamundaError] = deriveCodec
  given Schema[CamundaError] = Schema.derived
end CamundaError

case class CamundaAuthError(
    private val `type`: String = "AuthorizationException",
    message: String = "a detailed message",
    userId: String = "jonny",
    permissionName: String = "DELETE",
    resourceName: String = "User",
    resourceId: String = "Mary"
)
object CamundaAuthError:
  given Schema[CamundaAuthError] = Schema.derived
  given CirceCodec[CamundaAuthError] = deriveCodec
end CamundaAuthError

given Schema[StatusCode] = Schema(SchemaType.SInteger())
given Encoder[StatusCode] = Encoder.instance(st => st.code.asJson)
given Decoder[StatusCode] = (c: HCursor) => c.value.as[Int].map(StatusCode(_))


@description(
  "Same as ExecuteTimerIn."
)
case class GetActiveJobIn(
                           @description(
                             """
                               |The id of the process - you want to get the active tasks.
                               |> This is the result id of the `StartProcessOut`
                               |
                               |Add in the _Tests_ panel of _Postman_:
                               |```
                               |let result = pm.response.json();
                               |pm.collectionVariables.set("processInstanceId", result.id)
                               |```
                               |""".stripMargin
                           )
                           processInstanceId: String = "{{processInstanceId}}",
                           @description("We are only interested in the active Job(s)")
                           active: Boolean = true
                         )
object GetActiveJobIn:
  given Schema[GetActiveJobIn] = Schema.derived
  given CirceCodec[GetActiveJobIn] = deriveCodec

/*
@endpointInput("task/{taskId}/form-variables")
case class GetTaskFormVariablesPath(
                                    @pathParam
                                    taskId: String = "{{taskId}}",
                                    @query
                                    variableNames: Option[String] = None,
                                    @query
                                    deserializeValues: Boolean = true
                                  )
 */
@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class CompleteTaskIn(
    // use the description of the object
    variables: Map[ExampleName, CamundaVariable],
    @description(
      "Set to false will not return the Process Variables and the Result Status is 204."
    )
    withVariablesInReturn: Boolean = true
)
object CompleteTaskIn:
  given Schema[CompleteTaskIn] = Schema.derived
  given CirceCodec[CompleteTaskIn] = deriveCodec

@description(
  "Same as GetActiveJobIn."
)
case class GetActiveTaskIn(
    @description(
      """
        |The id of the process - you want to get the active tasks.
        |> This is the result id of the `StartProcessOut`
        |
        |Add in the _Tests_ panel of _Postman_:
        |```
        |let result = pm.response.json();
        |pm.collectionVariables.set("processInstanceId", result.id)
        |```
        |""".stripMargin
    )
    processInstanceId: String = "{{processInstanceId}}",
    @description("We are only interested in the active Task(s)")
    active: Boolean = true
)
object GetActiveTaskIn:
  given Schema[GetActiveTaskIn] = Schema.derived
  given CirceCodec[GetActiveTaskIn] = deriveCodec

case class RequestInput[T](examples: Map[ExampleName, T]):
  def :+(label: String, example: T): RequestInput[T] =
    copy(examples = examples + (label -> example))
  lazy val noInput: Boolean =
    examples.isEmpty

object RequestInput:
  def apply[T <: Product](example: T) =
    new RequestInput[T](Map("standard" -> example))

  def apply[T <: Product]() =
    new RequestInput[T](Map.empty)

case class RequestOutput[T](
    statusCode: StatusCode,
    examples: Map[ExampleName, T]
):
  lazy val noOutdput: Boolean =
    examples.isEmpty
  def :+(label: String, example: T): RequestOutput[T] =
    copy(examples = examples + (label -> example))

object RequestOutput {

  def apply[Out <: Product](): RequestOutput[Out] =
    new RequestOutput(StatusCode.Ok, Map.empty)

  def apply[Out <: Product](
      statusCode: StatusCode,
      example: Out
  ): RequestOutput[Out] =
    RequestOutput(statusCode, Map("standard" -> example))

  def ok[Out <: Product](
      example: Out
  ): RequestOutput[Out] =
    apply(StatusCode.Ok, example)

  def created[Out <: Product](
      example: Out
  ): RequestOutput[Out] =
    apply(StatusCode.Created, example)

  def ok[Out <: Product](
      examples: Map[ExampleName, Out]
  ): RequestOutput[Out] =
    RequestOutput(StatusCode.Ok, examples)

  def created[Out <: Product](
      examples: Map[ExampleName, Out]
  ): RequestOutput[Out] =
    RequestOutput(StatusCode.Created, examples)

}

@description(
  """A JSON object representing the newly created process instance.
    |""".stripMargin
)
case class StartProcessOut(
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: Map[ExampleName, CamundaVariable],
    @description(
      """The id of the process instance.
        |
        |> **Postman**:
        |>
        |> Add the following to the tests to set the `processInstanceId`:
        |>
        |>```
        |let processInstanceId = pm.response.json().id
        |console.log("processInstanceId: " + processInstanceId)
        |pm.collectionVariables.set("processInstanceId", processInstanceId)
        |>```
        |""".stripMargin
    )
    id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007",
    @description("The id of the process definition.")
    definitionId: String =
      "processDefinitionKey:1:6fe66514-12ea-11ec-936e-0242ac1d0007",
    @description("The business key of the process instance.")
    businessKey: Option[String] = Some("example-businesskey")
)

case class GetActiveTaskOut(
    @description(
      """The Task Id you need to complete Task
        |
        |> **Postman**:
        |>
        |> Add the following to the tests to set the `taskId`:
        |>
        |>```
        |let taskId = pm.response.json()[0].id
        |console.log("taskId: " + taskId)
        |pm.collectionVariables.set("taskId", taskId)
        |>```
        |>
        |> This returns an Array!
        |""".stripMargin
    )
    id: String = "f150c3f1-13f5-11ec-936e-0242ac1d0007"
)
object GetActiveTaskOut:
  given Schema[GetActiveTaskOut] = Schema.derived
  given CirceCodec[GetActiveTaskOut] = deriveCodec
end GetActiveTaskOut


