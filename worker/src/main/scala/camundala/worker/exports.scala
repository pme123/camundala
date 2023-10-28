package camundala
package worker

import camundala.domain.*
import camundala.bpmn.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.*

import java.net.URLDecoder
import java.nio.charset.Charset

private def decodeMock[Out: Decoder](
    isService: Boolean,
    json: Json
)(using
    context: EngineContext
): Either[MockerError | MockedOutput, Option[Out]] =
  (json.isObject, isService) match
    case (true, true) =>
      decodeTo[Out](json.asJson.toString)
        .map(Some(_))
        .left
        .map(ex => MockerError(errorMsg = ex.errorMsg))
    case (true, _) =>
      Left(
        MockedOutput(output = context.toEngineObject(json))
      )
    case _ =>
      Left(
        MockerError(errorMsg = s"The mock must be a Json Object:\n- $json\n- ${json.getClass}")
      )
end decodeMock

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

type HandledErrorCodes = Seq[ErrorCodeType]

sealed trait CamundalaWorkerError:
  def isMock = false
  def errorCode: ErrorCodeType
  def errorMsg: String

sealed trait ErrorWithOutput extends CamundalaWorkerError:
  def output: Map[String, Any]

object CamundalaWorkerError:

  case class CamundaBpmnError(errorCode: ErrorCodes, errorMsg: String)

  case class ValidatorError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`validation-failed`
  ) extends ErrorWithOutput:
    def output: Map[String, Any] =
      Map("validationErrors" -> errorMsg)

  case class MockedOutput(
      output: Map[String, Any],
      errorCode: ErrorCodes = ErrorCodes.`output-mocked`,
      errorMsg: String = "Output mocked"
  ) extends ErrorWithOutput:
    override val isMock = true

  case class CustomError(errorCode: String, errorMsg: String) extends CamundalaWorkerError

  case class InitProcessError(
      errorMsg: String = "Problems initialize default variables of the Process.",
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

  trait RunnerError extends CamundalaWorkerError

  trait ServiceError extends RunnerError

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

  def requestMsg[ServiceIn : Encoder](
      runnableRequest: RunnableRequest[ServiceIn]
  ): String =
    s""" - Request URL: ${URLDecoder.decode(
        runnableRequest.apiUri.toString,
        Charset.defaultCharset()
      )}
       | - Request Params: ${runnableRequest.queryParams
        .map {
          case k -> seq if seq.isEmpty =>
            k
          case k -> seq => s"$k=${seq.mkString(",")}"
        }
        .mkString("&")}
       | - Request Body: ${runnableRequest.requestBodyOpt
        .map(_.asJson)
        .getOrElse("")}""".stripMargin

  def serviceErrorMsg[ServiceIn  : Encoder](
      status: Int,
      errorMsg: String,
      runnableRequest: RunnableRequest[ServiceIn]
  ): String =
    s"""Service Error: $status
       |ErrorMsg: $errorMsg
       |${requestMsg(runnableRequest)}""".stripMargin
end CamundalaWorkerError

def niceClassName(clazz: Class[?]) =
  clazz.getName.split("""\$""").head
