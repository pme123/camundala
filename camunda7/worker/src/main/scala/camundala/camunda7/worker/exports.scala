package camundala
package camunda7.worker

import domain.*
import bpmn.*
import io.circe.*
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl

import java.net.URLDecoder
import java.nio.charset.Charset
import java.time.LocalDateTime

export sttp.model.{Method, Uri, QueryParams}
export org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
export org.springframework.context.annotation.Configuration

def toCamunda[T <: Product: Encoder](
    product: T
): Map[String, Any] =
  product.productElementNames
    .zip(product.productIterator)
    // .filterNot { case _ -> v => v.isInstanceOf[None.type] } // don't send null
    .map { case (k, v) => k -> objectToCamunda(product, k, v) }
    .toMap

def toCamunda(
    variables: Map[String, Json]
): Map[String, Any] =
  variables
    .map { case (k, v) => k -> camundaVariable(v) }

def objectToCamunda[T <: Product: Encoder](
    product: T,
    key: String,
    value: Any
): Any =
  value match
    case None | null => null
    case Some(v)     => objectToCamunda(product, key, v)
    case v: (Product | Iterable[?] | Map[?, ?]) =>
      product.asJson.deepDropNullValues.hcursor
        .downField(key)
        .as[Json] match {
        case Right(v) => camundaVariable(v)
        case Left(ex) =>
          throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
      }

    case v =>
      valueToCamunda(v)

def valueToCamunda(value: Any): Any =
  value match
    case v: scala.reflect.Enum =>
      v.toString
    case ldt: LocalDateTime =>
      ldt.toString
    case other if other == null =>
      null
    case v: Json =>
      camundaVariable(v)
    case other =>
      other

def camundaVariable[A <: Product: CirceCodec](variable: A): Any =
  new JsonValueImpl(variable.asJson.toString)

def camundaVariable(json: Json): Any =
  json match
    case j if j.isNull    => null
    case j if j.isNumber  => j.asNumber.get.toBigDecimal.get
    case j if j.isBoolean => j.asBoolean.get
    case j if j.isString  => j.asString.get
    case j =>
      new JsonValueImpl(j.toString)

end camundaVariable

def decodeTo[A: Decoder](
    jsonStr: String
): Either[CamundalaWorkerError.UnexpectedError, A] =
  io.circe.parser
    .decodeAccumulating[A](jsonStr)
    .toEither
    .left
    .map { ex =>
      CamundalaWorkerError.UnexpectedError(errorMsg =
        ex.toList
          .map(_.getMessage())
          .mkString(
            "Decoding Error: Json is not valid:\n - ",
            "\n - ",
            s"\n * Json: $jsonStr\n"
          )
      )
    }
end decodeTo

type HelperContext[T] = ExternalTask ?=> T

type HandledErrorCodes = Seq[ErrorCodeType]

sealed trait CamundalaWorkerError:
  def isMock = false
  def errorCode: ErrorCodeType
  def errorMsg: String

sealed trait ErrorWithOutput extends CamundalaWorkerError:
  def mockedOutput: Map[String, Any]

object CamundalaWorkerError:

  case class CamundaBpmnError(errorCode: ErrorCodes, errorMsg: String)

  case class ValidatorError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`validation-failed`
  ) extends ErrorWithOutput:
    def mockedOutput: Map[String, Any] =
      Map("validationErrors" -> errorMsg)

  case class MockedOutput(
      mockedOutput: Map[String, Any],
      errorCode: ErrorCodes = ErrorCodes.`output-mocked`,
      errorMsg: String = "Output mocked"
  ) extends ErrorWithOutput:
    override val isMock = true

  case class CustomError(errorCode: String, errorMsg: String)
      extends CamundalaWorkerError

  case class InitializerError(
      errorMsg: String =
        "Problems initialize default variables of the Process.",
      errorCode: ErrorCodes = ErrorCodes.`error-unexpected`
  ) extends CamundalaWorkerError

  case class MockerError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`mocking-failed`
  ) extends CamundalaWorkerError

  case class MappingError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`mapping-error`
  ) extends CamundalaWorkerError

  case class UnexpectedError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`error-unexpected`
  ) extends CamundalaWorkerError

  case class HandledRegexNotMatchedError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`error-handledRegexNotMatched`
  ) extends CamundalaWorkerError

  object HandledRegexNotMatchedError:
    def apply(error: CamundalaWorkerError): HandledRegexNotMatchedError =
      HandledRegexNotMatchedError(
        s"""The error was handled, but did not match the defined 'regexHandledErrors'.
          |Original Error: ${error.errorCode} - ${error.errorMsg}
          |""".stripMargin
      )

  case class BadVariableError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`bad-variable`
  ) extends CamundalaWorkerError

  trait ServiceError extends CamundalaWorkerError

  case class ServiceAuthError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-auth-error`
  ) extends ServiceError

  case class ServiceBadBodyError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-bad-body-error`
  ) extends ServiceError
  case class ServiceUnexpectedError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-unexpected-error`
  ) extends ServiceError
  case class ServiceRequestError(
      errorCode: Int,
      errorMsg: String
  ) extends ServiceError

  def requestMsg[ServiceIn: Encoder](
      apiUri: Uri,
      queryParams: Seq[(String, Seq[String])],
      requestBody: Option[ServiceIn]
  ): String =
    s""" - Request URL: ${URLDecoder.decode(apiUri.toString, Charset.defaultCharset())}
       | - Request Params: ${
        queryParams
          .map {
            case k -> seq if seq.isEmpty =>
              k
            case k -> seq => s"$k=${seq.mkString(",")}"
          }
          .mkString("&")}
       | - Request Body: ${requestBody.map(_.asJson).getOrElse("")}""".stripMargin

  def serviceErrorMsg[ServiceIn: Encoder](
      status: Int,
      errorMsg: String,
      apiUri: Uri,
      queryParams: Seq[(String, Seq[String])],
      requestBody: Option[ServiceIn]
  ): String =
    s"""Service Error: $status
       |ErrorMsg: $errorMsg
       |${requestMsg(apiUri, queryParams, requestBody)}""".stripMargin
end CamundalaWorkerError

def niceClassName(clazz: Class[?]) =
  clazz.getName.split("""\$""").head
