package camundala
package api

import io.circe.generic.auto.*
import io.circe.syntax.*
import io.circe.*
import sttp.model.StatusCode
import sttp.tapir.Schema
import sttp.tapir.Schema.annotations.description
import sttp.tapir.generic.auto.*

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
            .as[Json]
            .toOption
            .map(_.toString)
            .getOrElse(s"$v could NOT be Parsed to a JSON!")
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

case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

@description(
  "A JSON object with the following properties: (at least an empty JSON object {} or an empty request body)"
)
case class StartProcessIn(
    // use the description of the object
    variables: Map[ExampleName, CamundaVariable],
    @description("The business key of the process instance.")
    businessKey: Option[String] = Some("example-businesskey"),
    @description("Set to false will not return the Process Variables.")
    withVariablesInReturn: Boolean = true
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
      """The id of the process - you want to get the active tasks.
        |> This is the result id of the `StartProcessOut`""".stripMargin
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
