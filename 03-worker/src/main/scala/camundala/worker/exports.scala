package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.*

import java.util.Date

export sttp.model.Uri.UriContext
export sttp.model.Method
export sttp.model.Uri

type SendRequestType[ServiceOut] =
  EngineRunContext ?=> Either[ServiceError, ServiceResponse[ServiceOut]]

def decodeTo[A: InOutDecoder](
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
            s"\n * Json: ${jsonStr.take(3500)}\n" // 4000 throws error in Camunda.
          )
      )
    }
end decodeTo

type HandledErrorCodes = Seq[ErrorCodeType]

sealed trait CamundalaWorkerError extends Throwable:
  def isMock = false
  def errorCode: ErrorCodeType
  def errorMsg: String

  def causeMsg = s"$errorCode: $errorMsg"
end CamundalaWorkerError

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
  end ValidatorError

  case class MockedOutput(
      output: Map[String, Any],
      errorCode: ErrorCodes = ErrorCodes.`output-mocked`,
      errorMsg: String = "Output mocked"
  ) extends ErrorWithOutput:
    override val isMock = true
  end MockedOutput

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
  end HandledRegexNotMatchedError

  case class BadVariableError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`bad-variable`
  ) extends CamundalaWorkerError

  sealed trait RunWorkError extends CamundalaWorkerError

  case class CustomError(errorMsg: String, errorCode: ErrorCodes = ErrorCodes.`custom-run-error`)
      extends RunWorkError

  trait ServiceError extends RunWorkError

  case class ServiceMappingError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-mapping-error`
  ) extends ServiceError
  case class ServiceMockingError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-mocking-error`
  ) extends ServiceError
  case class ServiceBadPathError(
      errorMsg: String,
      errorCode: ErrorCodes = ErrorCodes.`service-bad-path-error`
  ) extends ServiceError

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

  def requestMsg[ServiceIn: InOutEncoder](
      runnableRequest: RunnableRequest[ServiceIn]
  ): String =
    s""" - Request URL: ${prettyUriString(
        runnableRequest.apiUri.addQuerySegments(runnableRequest.qSegments)
      )}
       | - Request Body: ${runnableRequest.requestBodyOpt
        .map(_.asJson.deepDropNullValues)
        .getOrElse("")}
       | - Request Header: ${runnableRequest.headers.map { case k -> v => s"$k -> $v" }.mkString(
        ", "
      )}""".stripMargin

  def serviceErrorMsg[ServiceIn: InOutEncoder](
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

def printTimeOnConsole(start: Date) =
  val time  = new Date().getTime - start.getTime
  val color = if time > 1000 then Console.YELLOW_B
  else if time > 250 then Console.MAGENTA
  else Console.BLACK
  s"($color$time ms${Console.RESET})"
end printTimeOnConsole
