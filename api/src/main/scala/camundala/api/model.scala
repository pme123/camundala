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

case class CamundaError(
    `type`: String = "SomeExceptionClass",
    message: String = "a detailed message"
)

case class CamundaAuthError(
    private val `type`: String = "AuthorizationException",
    message: String = "a detailed message",
    userId: String = "jonny",
    permissionName: String = "DELETE",
    resourceName: String = "User",
    resourceId: String = "Mary"
)

given Schema[StatusCode] = Schema(SchemaType.SInteger())
given Encoder[StatusCode] = Encoder.instance(st => st.code.asJson)
given Decoder[StatusCode] = (c: HCursor) => c.value.as[Int].map(StatusCode(_))

given Schema[RequestErrorOutput] = Schema.derived
given CirceCodec[RequestErrorOutput] = deriveCodec
given Schema[CamundaError] = Schema.derived
given CirceCodec[CamundaError] = deriveCodec
given Schema[CamundaAuthError] = Schema.derived
given CirceCodec[CamundaAuthError] = deriveCodec

@description(
  """Output for /history/variable-instance?processInstanceIdIn=#{processInstanceId}
    |```
    |  {
    |    "type": "Boolean",
    |    "value": true,
    |    "valueInfo": {},
    |    "id": "0f99b629-6b9a-11ec-8318-6a9c8e2a273d",
    |    "name": "clarified",
    |    "processDefinitionKey": "ReviewInvoiceProcess",
    |    "processDefinitionId": "ReviewInvoiceProcess:6:88718f1d-6297-11ec-8d87-6a9c8e2a273d",
    |    "processInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "executionId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "activityInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d",
    |    "caseDefinitionKey": null,
    |    "caseDefinitionId": null,
    |    "caseInstanceId": null,
    |    "caseExecutionId": null,
    |    "taskId": null,
    |    "errorMessage": null,
    |    "tenantId": null,
    |    "state": "CREATED",
    |    "createTime": "2022-01-02T08:03:17.251+0100",
    |    "removalTime": null,
    |    "rootProcessInstanceId": "0e4ff801-6b9a-11ec-8318-6a9c8e2a273d"
    |  }
    |```
    |""".stripMargin
)
case class CamundaProperty(
    key: String,
    value: CamundaVariable
)

object CamundaProperty:

  implicit val decodeGetHistoryVariablesOut: Decoder[CamundaProperty] =
    (c: HCursor) =>
      for
        name <- c.downField("name").as[String]
        valueType <- c.downField("type").as[String]
        anyValue = c.downField("value")
        value <- CamundaVariable.decodeValue(
          valueType,
          anyValue,
          c.downField("valueInfo")
        )
      yield new CamundaProperty(name, value)

  def from(vars: FormVariables): Seq[CamundaProperty] =
    vars.map { case k -> c =>
      CamundaProperty(k, c)
    }.toSeq

end CamundaProperty

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn(
    // use the description of the object
    variables: Map[String, CamundaVariable],
    @description("The business key of the process instance.")
    businessKey: Option[String] = Some("example-businesskey"),
    @description("Set to false will not return the Process Variables.")
    withVariablesInReturn: Boolean = true
)
object StartProcessIn:
  given Schema[StartProcessIn] = Schema.derived
  given CirceCodec[StartProcessIn] = deriveCodec

case class CorrelateMessageIn(
    messageName: String,
    businessKey: Option[String] = None,
    tenantId: Option[String] = None,
    withoutTenantId: Option[Boolean] = None,
    processInstanceId: Option[String] = None,
    correlationKeys: Option[FormVariables] = None,
    localCorrelationKeys: Option[FormVariables] = None,
    processVariables: Option[FormVariables] = None,
    processVariablesLocal: Option[FormVariables] = None,
    all: Boolean = false,
    resultEnabled: Boolean = true,
    variablesInResultEnabled: Boolean = true
)
object CorrelateMessageIn:
  given Schema[CorrelateMessageIn] = Schema.derived
  given CirceCodec[CorrelateMessageIn] = deriveCodec

case class SendSignalIn(
    @description("The name of the signal to deliver.")
    name: String,
    @description(
      """
Specifies a tenant to deliver the signal. The signal can only be received on executions or process definitions which belongs to the given tenant.

Note: Cannot be used in combination with executionId.
"""
    )
    tenantId: Option[String] = None,
    withoutTenantId: Option[Boolean] = None,
    @description("""
Optionally specifies a single execution which is notified by the signal.

Note: If no execution id is defined the signal is broadcasted to all subscribed handlers.
""")
    executionId: Option[String] = None,
    @description(
      """A JSON object containing variable key-value pairs. Each key is a variable name and each value a JSON variable value object."""
    )
    variables: Option[Map[String, CamundaVariable]] = None
)

object SendSignalIn:
  given Schema[SendSignalIn] = Schema.derived
  given CirceCodec[SendSignalIn] = deriveCodec

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

@description(
  "A JSON object with the following properties:"
)
case class EvaluateDecisionIn(
    // use the description of the object
    variables: Map[ExampleName, CamundaVariable]
)
object EvaluateDecisionIn:
  given Schema[EvaluateDecisionIn] = Schema.derived
  given CirceCodec[EvaluateDecisionIn] = deriveCodec

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

case class CompleteTaskOut(
    @description(
      "The Process Variables - Be aware that returns everything stored in the Process."
    )
    variables: Map[ExampleName, CamundaVariable]
)
object CompleteTaskOut:
  given Schema[CompleteTaskOut] = Schema.derived
  given CirceCodec[CompleteTaskOut] = deriveCodec
end CompleteTaskOut

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

@description(
  """A JSON object containing a property for each variable returned. The key is the variable name,
    |the value is a JSON object with the following properties:
    |```
    |{
    |  "amount": {
    |    "type": "Double",
    |    "value": 300.0,
    |    "valueInfo": {}
    |  }
    |}
    |```
    |""".stripMargin
)
type FormVariables = Map[String, CamundaVariable]

