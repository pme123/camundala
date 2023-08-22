package camundala
package bpmn

import domain.*

import io.circe.syntax.*
import scala.compiletime.{constValue, constValueTuple}

val camundaVersion = "7.15"

// os
export os.{pwd, Path, ResourcePath, read}

// sttp
export sttp.model.StatusCode
  
def toJsonString[T <: Product: Encoder](product: T): String =
  product.asJson.deepDropNullValues.toString

@deprecated("Use `Optable`.")
def maybe[T](value: T | Option[T]): Option[T] = value match
  case v: Option[?] => v.asInstanceOf[Option[T]]
  case v => Some(v.asInstanceOf[T])

def cawemoDescr(descr: String, cawemoLink: String) =
  s"""
     |$descr
     |
     |<iframe src="https://cawemo.com/embed/$cawemoLink" style="width:100%;height:500px;border:1px solid #ccc" allowfullscreen></iframe>
     |""".stripMargin

inline def nameOfVariable(inline x: Any): String = ${ NameFromVariable.nameOfVariable('x) }

enum InputParams:
  case servicesMocked
  case outputMock
  case mockedSubprocesses
  case outputServiceMock
  case outputVariables
  case handledErrors
  case regexHandledErrors
  case impersonateUserId
end InputParams

enum ErrorCodes:
  case `output-mocked` // mocking successful - but the mock is sent as BpmnError to handle in the diagram correctly
  case `mocking-failed`
  case `validation-failed`
  case `error-unexpected`
  case `running-failed`
  case `bad-variable`
  case `mapping-error`
  case `service-auth-error`
  case `service-bad-body-error`
  case `service-unexpected-error`
end ErrorCodes

val GenericServiceProcessName = "camundala-service-generic"