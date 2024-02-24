package camundala
package bpmn

import camundala.domain.*

import scala.compiletime.{constValue, constValueTuple}

val camundaVersion = "7.15"

// sttp
export sttp.model.StatusCode

def toJsonString[T <: Product: InOutEncoder](product: T): String =
  product.asJson.deepDropNullValues.toString

@deprecated("Use `Optable`.")
def maybe[T](value: T | Option[T]): Option[T] = value match
  case v: Option[?] => v.asInstanceOf[Option[T]]
  case v => Some(v.asInstanceOf[T])

inline def nameOfVariable(inline x: Any): String = ${ NameOf.nameOfVariable('x) }
inline def nameOfType[A]: String = ${ NameOf.nameOfType[A] }

enum InputParams:
  // mocking
  case servicesMocked
  case mockedWorkers
  case outputMock
  case outputServiceMock
  // mapping
  case manualOutMapping
  case outputVariables
  case handledErrors
  case regexHandledErrors
  // authorization
  case impersonateUserId
  // special cases
  case topicName
  case inConfig
end InputParams

type ErrorCodeType = ErrorCodes | String | Int

enum ErrorCodes:
  case `output-mocked` // mocking successful - but the mock is sent as BpmnError to handle in the diagram correctly
  case `mocking-failed`
  case `validation-failed`
  case `error-unexpected`
  case `error-handledRegexNotMatched`
  case `running-failed`
  case `bad-variable`
  case `mapping-error`
  case `custom-run-error`
  case `service-mapping-error`
  case `service-mocking-error`
  case `service-bad-path-error`
  case `service-auth-error`
  case `service-bad-body-error`
  case `service-unexpected-error`
end ErrorCodes

val GenericExternalTaskProcessName = "camundala-externalTask-generic"

object GenericExternalTask:
  enum ProcessStatus:
    case succeeded, `404`, `400`, `output-mocked`, `validation-failed`
  object ProcessStatus:
    given ApiSchema[ProcessStatus] = deriveEnumApiSchema
    given InOutCodec[ProcessStatus] = deriveEnumInOutCodec
end GenericExternalTask

trait WithConfig[InConfig <: Product: InOutCodec]:
  def inConfig: Option[InConfig]
  def defaultConfig: InConfig

case class NoInConfig()

object NoInConfig:
  given InOutCodec[NoInConfig] = deriveCodec
  given ApiSchema[NoInConfig] = deriveApiSchema

// ApiCreator that describes these variables
case class GeneralVariables(
    // mocking
    servicesMocked: Boolean = false, // Process only
    mockedWorkers: Seq[String] = Seq.empty, // Process only
    outputMock: Option[Json] = None,
    outputServiceMock: Option[Json] = None, // Service only
    // mapping
    manualOutMapping: Boolean = false, // Service only
    outputVariables: Seq[String] = Seq.empty, // Service only
    handledErrors: Seq[String] = Seq.empty, // Service only
    regexHandledErrors: Seq[String] = Seq.empty, // Service only
    // authorization
    impersonateUserId: Option[String] = None
):
  def isMockedWorker(workerTopicName: String): Boolean =
    mockedWorkers.contains(workerTopicName)

object GeneralVariables:
  given InOutCodec[GeneralVariables] = deriveCodec
  given ApiSchema[GeneralVariables] = deriveApiSchema
end GeneralVariables
