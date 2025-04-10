package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.*
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}
import zio.{IO, ZIO}

import java.util.Date

export sttp.model.Uri.UriContext
export sttp.model.Method
export sttp.model.Uri

export zio.ZIO
export zio.IO

lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

type SendRequestType[ServiceOut] =
  EngineRunContext ?=> IO[ServiceError, ServiceResponse[ServiceOut]]

def decodeTo[A: InOutDecoder](
    jsonStr: String
): IO[CamundalaWorkerError.UnexpectedError, A] =
  ZIO.fromEither(io.circe.parser
    .decodeAccumulating[A](jsonStr)
    .toEither)
    .mapError { ex =>
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

  def causeMsg                                   = s"$errorCode: $errorMsg"
  def causeError: Option[CamundalaWorkerError]   = None
  def generalVariables: Option[GeneralVariables] = None
  override def toString(): String                =
    causeMsg + causeError.map(e => s"Caused by ${e.causeMsg}").getOrElse("")
end CamundalaWorkerError

sealed trait ErrorWithOutput extends CamundalaWorkerError:
  def output: Map[String, Any]

object CamundalaWorkerError:

  case class CamundaBpmnError(errorCode: ErrorCodes, errorMsg: String)

  case class ValidatorError(
      errorMsg: String
  ) extends ErrorWithOutput:
    val errorCode: ErrorCodes    = ErrorCodes.`validation-failed`
    def output: Map[String, Any] = Map("validationErrors" -> errorMsg)
  end ValidatorError

  case class MockedOutput(
      output: Map[String, Any],
      errorMsg: String = "Output mocked"
  ) extends ErrorWithOutput:
    val errorCode: ErrorCodes = ErrorCodes.`output-mocked`
    override val isMock       = true
  end MockedOutput

  case object AlreadyHandledError extends CamundalaWorkerError:
    val errorMsg: String      = "Error already handled."
    val errorCode: ErrorCodes = ErrorCodes.`error-already-handled`

  case class InitProcessError(
      errorMsg: String = "Problems initialize default variables of the Process."
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`error-unexpected`

  case class MockerError(
      errorMsg: String
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`mocking-failed`

  case class MappingError(
      errorMsg: String
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`mapping-error`

  case class UnexpectedError(
      errorMsg: String
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`error-unexpected`

  case class HandledRegexNotMatchedError(
      errorMsg: String
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`error-handledRegexNotMatched`

  object HandledRegexNotMatchedError:
    def apply(error: CamundalaWorkerError): HandledRegexNotMatchedError =
      HandledRegexNotMatchedError(
        s"""The error was handled, but did not match the defined 'regexHandledErrors'.
           |Original Error: ${error.errorCode} - ${error.errorMsg}
           |""".stripMargin
      )
  end HandledRegexNotMatchedError

  case class BadVariableError(
      errorMsg: String
  ) extends CamundalaWorkerError:
    val errorCode: ErrorCodes = ErrorCodes.`bad-variable`

  sealed trait RunWorkError extends CamundalaWorkerError

  case class MissingHandlerError(
      errorMsg: String
  ) extends RunWorkError:
    val errorCode: ErrorCodes = ErrorCodes.`running-failed`

  case class CustomError(
      errorMsg: String,
      override val generalVariables: Option[GeneralVariables] = None,
      override val causeError: Option[CamundalaWorkerError] = None
  ) extends RunWorkError:
    val errorCode: ErrorCodes = ErrorCodes.`custom-run-error`
  end CustomError

  trait ServiceError extends RunWorkError

  case class ServiceMappingError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-mapping-error`

  case class ServiceMockingError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-mocking-error`

  case class ServiceBadPathError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-bad-path-error`

  case class ServiceAuthError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-auth-error`

  case class ServiceBadBodyError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-bad-body-error`

  case class ServiceUnexpectedError(
      errorMsg: String
  ) extends ServiceError:
    val errorCode: ErrorCodes = ErrorCodes.`service-unexpected-error`

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
