package camundala
package api

import domain.*
import bpmn.*
import io.circe.syntax.*
import io.circe.*
import sttp.model.StatusCode
import sttp.tapir.{Schema, SchemaType}
import sttp.tapir.Schema.annotations.description

import java.util.Base64
import scala.annotation.tailrec
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

implicit lazy val StatusCodeSchema: Schema[StatusCode] =
  Schema(SchemaType.SInteger())
implicit lazy val StatusCodeEncoder: Encoder[StatusCode] =
  Encoder.instance(st => st.code.asJson)
implicit lazy val StatusCodeDecoder: Decoder[StatusCode] =
  (c: HCursor) => c.value.as[Int].map(StatusCode(_))

implicit lazy val RequestErrorOutputSchema: Schema[RequestErrorOutput] =
  Schema.derived
implicit lazy val RequestErrorOutputEncoder: Encoder[RequestErrorOutput] =
  deriveEncoder
implicit lazy val RequestErrorOutputDecoder: Decoder[RequestErrorOutput] =
  deriveDecoder
implicit lazy val CamundaErrorSchema: Schema[CamundaError] = Schema.derived
implicit lazy val CamundaErrorEncoder: Encoder[CamundaError] = deriveEncoder
implicit lazy val CamundaErrorDecoder: Decoder[CamundaError] = deriveDecoder
implicit lazy val CamundaAuthErrorSchema: Schema[CamundaAuthError] =
  Schema.derived
implicit lazy val CamundaAuthErrorEncoder: Encoder[CamundaAuthError] =
  deriveEncoder
implicit lazy val CamundaAuthErrorDecoder: Decoder[CamundaAuthError] =
  deriveDecoder

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

sealed trait CamundaVariable:
  def value: Any

object CamundaVariable:

  implicit val encodeCamundaVariable: Encoder[CamundaVariable] =
    Encoder.instance {
      case v: CString => v.asJson
      case v: CInteger => v.asJson
      case v: CLong => v.asJson
      case v: CDouble => v.asJson
      case v: CBoolean => v.asJson
      case v: CFile => v.asJson
      case v: CJson => v.asJson
      case v: CEnum => v.asJson
      case CNull => Json.Null
    }

  implicit lazy val CamundaVariableSchema: Schema[CamundaVariable] =
    Schema.derived

  implicit lazy val CStringSchema: Schema[CString] = Schema.derived
  implicit lazy val CStringEncoder: Encoder[CString] = deriveEncoder
  implicit lazy val CStringDecoder: Decoder[CString] = deriveDecoder

  implicit lazy val CIntegerSchema: Schema[CInteger] = Schema.derived
  implicit lazy val CIntegerEncoder: Encoder[CInteger] = deriveEncoder
  implicit lazy val CIntegerDecoder: Decoder[CInteger] = deriveDecoder

  implicit lazy val CLongSchema: Schema[CLong] = Schema.derived
  implicit lazy val CLongEncoder: Encoder[CLong] = deriveEncoder
  implicit lazy val CLongDecoder: Decoder[CLong] = deriveDecoder

  implicit lazy val CDoubleSchema: Schema[CDouble] = Schema.derived
  implicit lazy val CDoubleEncoder: Encoder[CDouble] = deriveEncoder
  implicit lazy val CDoubleDecoder: Decoder[CDouble] = deriveDecoder

  implicit lazy val CBooleanSchema: Schema[CBoolean] = Schema.derived
  implicit lazy val CBooleanEncoder: Encoder[CBoolean] = deriveEncoder
  implicit lazy val CBooleanDecoder: Decoder[CBoolean] = deriveDecoder

  implicit lazy val CFileSchema: Schema[CFile] = Schema.derived
  implicit lazy val CFileEncoder: Encoder[CFile] = deriveEncoder
  implicit lazy val CFileDecoder: Decoder[CFile] = deriveDecoder

  implicit lazy val CFileValueInfoSchema: Schema[CFileValueInfo] =
    Schema.derived
  implicit lazy val CFileValueInfoEncoder: Encoder[CFileValueInfo] =
    deriveEncoder
  implicit lazy val CFileValueInfoDecoder: Decoder[CFileValueInfo] =
    deriveDecoder

  implicit lazy val CJsonSchema: Schema[CJson] = Schema.derived
  implicit lazy val CJsonEncoder: Encoder[CJson] = deriveEncoder
  implicit lazy val CJsonDecoder: Decoder[CJson] = deriveDecoder

  implicit lazy val CEnumSchema: Schema[CEnum] = Schema.derived
  implicit lazy val CEnumEncoder: Encoder[CEnum] = deriveEncoder
  implicit lazy val CEnumDecoder: Decoder[CEnum] = deriveDecoder

  import reflect.Selectable.reflectiveSelectable

  def toCamunda[T <: Product: Encoder](
      product: T
  ): Map[ExampleName, CamundaVariable] =
    product.productElementNames
      .zip(product.productIterator)
      .filterNot { case k -> v => v.isInstanceOf[None.type] } // don't send null
      .map { case (k, v) => k -> objectToCamunda(product, k, v) }
      .toMap

  @tailrec
  def objectToCamunda[T <: Product: Encoder](
      product: T,
      key: String,
      value: Any
  ): CamundaVariable =
    value match
      case None => CNull
      case Some(v) => objectToCamunda(product, key, v)
      case f @ FileInOut(fileName, _, mimeType) =>
        CFile(
          f.contentAsBase64,
          CFileValueInfo(
            fileName,
            mimeType
          )
        )
      case v: Product if !v.isInstanceOf[scala.reflect.Enum] =>
        CJson(
          product.asJson.deepDropNullValues.hcursor
            .downField(key)
            .as[Json] match
            case Right(v) => v.toString
            case Left(ex) => throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
        )
      case v =>
        valueToCamunda(v)

  def valueToCamunda(value: Any): CamundaVariable =
    value match
      case v: String =>
        CString(v)
      case v: Int =>
        CInteger(v)
      case v: Long =>
        CLong(v)
      case v: Boolean =>
        CBoolean(v)
      case v: Float =>
        CDouble(v.toDouble)
      case v: Double =>
        CDouble(v)
      case v: scala.reflect.Enum =>
        CEnum(v.toString)
      case other if other == null =>
        CNull

  case object CNull extends CamundaVariable:
    val value: Null = null

    private val `type`: String = "String"
  case class CString(value: String, private val `type`: String = "String")
      extends CamundaVariable
  case class CInteger(value: Int, private val `type`: String = "Integer")
      extends CamundaVariable
  case class CLong(value: Long, private val `type`: String = "Long")
      extends CamundaVariable
  case class CBoolean(value: Boolean, private val `type`: String = "Boolean")
      extends CamundaVariable

  case class CDouble(value: Double, private val `type`: String = "Double")
      extends CamundaVariable

  case class CFile(
      @description("The File's content as Base64 encoded String.")
      value: String,
      valueInfo: CFileValueInfo,
      private val `type`: String = "File"
  ) extends CamundaVariable

  case class CFileValueInfo(
      filename: String,
      mimetype: Option[String]
  )

  case class CEnum(value: String, private val `type`: String = "String")
      extends CamundaVariable

  case class CJson(value: String, private val `type`: String = "Json")
      extends CamundaVariable

  implicit val decodeCamundaVariable: Decoder[CamundaVariable] =
    (c: HCursor) =>
      for
        valueType <- c.downField("type").as[String]
        anyValue = c.downField("value")
        value <- decodeValue(valueType, anyValue, c.downField("valueInfo"))
      yield value

  def decodeValue(
      valueType: String,
      anyValue: ACursor,
      valueInfo: ACursor
  ): Either[DecodingFailure, CamundaVariable] =
    valueType match
      case "Null" => Right(CNull)
      case "Boolean" => anyValue.as[Boolean].map(CBoolean(_))
      case "Integer" => anyValue.as[Int].map(CInteger(_))
      case "Long" => anyValue.as[Long].map(CLong(_))
      case "Double" => anyValue.as[Double].map(CDouble(_))
      case "Json" => anyValue.as[String].map(CJson(_))
      case "File" =>
        valueInfo.as[CFileValueInfo].map(vi => CFile("not_set", vi))
      case _ => anyValue.as[String].map(CString(_))

end CamundaVariable

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

case class CorrelateMessageIn(
    messageName: String,
    businessKey: Option[String] = None,
    tenantId: Option[String] = None,
    withoutTenantId: Option[Boolean] = None,
    processInstanceId: Option[String] = None,
    correlationKeys: Option[Map[String, CamundaVariable]] = None,
    localCorrelationKeys: Option[Map[String, CamundaVariable]] = None,
    processVariables: Option[Map[String, CamundaVariable]] = None,
    processVariablesLocal: Option[Map[String, CamundaVariable]] = None,
    all: Boolean = false,
    resultEnabled: Boolean = true,
    variablesInResultEnabled: Boolean = true
)

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

@description(
  "A JSON object with the following properties"
)
case class GetActiveTaskIn(
    @description(
      """
        |The id of the process - you want to get the active tasks.
        |> This is the result id of the `StartProcessOut`
        |"""
    )
    processInstanceId: String = "{{processInstanceId}}",
    @description("We are only interested in the active Task(s)")
    active: Boolean = true
)

@description(
  "A JSON object with the following properties:"
)
case class EvaluateDecisionIn(
    // use the description of the object
    variables: Map[ExampleName, CamundaVariable]
)

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

implicit lazy val StartProcessInSchema: Schema[StartProcessIn] = Schema.derived
implicit lazy val StartProcessInEncoder: Encoder[StartProcessIn] = deriveEncoder
implicit lazy val StartProcessInDecoder: Decoder[StartProcessIn] = deriveDecoder
implicit lazy val CorrelationMessageInSchema: Schema[CorrelateMessageIn] =
  Schema.derived
implicit lazy val CorrelationMessageInEncoder: Encoder[CorrelateMessageIn] =
  deriveEncoder
implicit lazy val CorrelationMessageInDecoder: Decoder[CorrelateMessageIn] =
  deriveDecoder
implicit lazy val SendSignalInSchema: Schema[SendSignalIn] = Schema.derived
implicit lazy val SendSignalInEncoder: Encoder[SendSignalIn] = deriveEncoder
implicit lazy val SendSignalInDecoder: Decoder[SendSignalIn] = deriveDecoder

implicit lazy val CompleteTaskInSchema: Schema[CompleteTaskIn] = Schema.derived
implicit lazy val CompleteTaskInEncoder: Encoder[CompleteTaskIn] = deriveEncoder
implicit lazy val CompleteTaskInDecoder: Decoder[CompleteTaskIn] = deriveDecoder
implicit lazy val GetActiveTaskInSchema: Schema[GetActiveTaskIn] =
  Schema.derived
implicit lazy val GetActiveTaskInEncoder: Encoder[GetActiveTaskIn] =
  deriveEncoder
implicit lazy val GetActiveTaskInDecoder: Decoder[GetActiveTaskIn] =
  deriveDecoder

implicit lazy val CompleteTaskOutSchema: Schema[CompleteTaskOut] =
  Schema.derived
implicit lazy val CompleteTaskOutEncoder: Encoder[CompleteTaskOut] =
  deriveEncoder
implicit lazy val CompleteTaskOutDecoder: Decoder[CompleteTaskOut] =
  deriveDecoder
