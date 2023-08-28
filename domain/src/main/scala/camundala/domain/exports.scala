package camundala
package domain

import io.circe.derivation.Configuration
import io.circe.parser
import io.circe.syntax.*

import java.util.Base64
import scala.language.implicitConversions

// circe
export io.circe.{Codec as CirceCodec, Decoder, Encoder, Json}
export io.circe.generic.semiauto.{deriveCodec, deriveDecoder, deriveEncoder}

// Circe Enum codec
// used implicit instead of given - so no extra import is needed domain.{*, given}
implicit val c: Configuration = Configuration.default.withDefaults
  .withDiscriminator("type")

export io.circe.derivation.ConfiguredCodec
export io.circe.derivation.ConfiguredEnumCodec

// Circe JSON
export sttp.tapir.json.circe.circeCodec
export sttp.tapir.json.circe.schemaForCirceJson
export sttp.tapir.json.circe.schemaForCirceJsonObject

// tapir
export sttp.tapir.Schema
export sttp.tapir.Schema.annotations.description

case class NoInput()
object NoInput:
  given Schema[NoInput] = Schema.derived
  given CirceCodec[NoInput] = deriveCodec

case class NoOutput()
object NoOutput:
  given Schema[NoOutput] = Schema.derived
  given CirceCodec[NoOutput] = deriveCodec

enum NotValidStatus derives ConfiguredEnumCodec:
  case notValid
object NotValidStatus:
  given Schema[NotValidStatus] = Schema.derived

enum CanceledStatus derives ConfiguredEnumCodec:
  case canceled
object CanceledStatus:
  given Schema[CanceledStatus] = Schema.derived


trait GenericServiceIn:
  def serviceName: String

case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

object FileInOut:
  given Schema[FileInOut] = Schema.derived
  given CirceCodec[FileInOut] = deriveCodec

/** In Camunda 8 only json is allowed!
  */
case class FileRefInOut(
    fileName: String,
    @description("A reference to retrieve the file in your application.")
    ref: String,
    mimeType: Option[String]
)

object FileRefInOut:
  given Schema[FileRefInOut] = Schema.derived
  given CirceCodec[FileRefInOut] = deriveCodec

// Use this in the DSL to avoid Option[?]
// see https://stackoverflow.com/a/69925310/2750966
case class Optable[Out](value: Option[Out])

object Optable {
  implicit def fromOpt[T](o: Option[T]): Optable[T] = Optable(o)
  implicit def fromValue[T](v: T): Optable[T] = Optable(Option(v))
}

//json
def throwErr(err: String) =
  println(s"ERROR: $err")
  throw new IllegalArgumentException(err)

def toJson(json: String): Json =
  parser.parse(json) match
    case Right(v) => v.deepDropNullValues
    case Left(exc) =>
      throwErr(s"Could not create Json from your String -> $exc")
val testModeDescr =
  "This flag indicades that this is a test - in the process it can behave accordingly."

// descriptions
def serviceNameDescr(serviceName: String) =
  s"As this uses the generic Service you need to name the Service to '$serviceName'."

def outputMockDescr[Out: CirceCodec, Schema](mock: Out) =
  s"""You can mock the response variables of this (sub)process.
     |
     |Class: `${mock.getClass.getName.replace("$", " > ")}`
     |
     |Here an example:
     |
     |```scala
     |${mock.asJson}
     |```
     |
     |General to mocking:
     |
     |- `outputMock` mocks this process.
     |- `someSubProcessMock` mocks a sub process
     |""".stripMargin

val servicesMockedDescr =
  "This flag will mock every Service that this Process calls, using the default Mock."

def outputServiceMockDescr[ServiceOut: Encoder](mock: ServiceOut) =
  s"""You can mock the response variables of this Http Service.
     |
     |Class: `${mock.getClass.getName.replace("$", " > ")}`
     |
     |Here an example:
     |
     |```scala
     |MockedHttpResponse.success200(
     |  ${mock.asJson}
     |)
     |```
     |
     |General to mocking:
     |
     |- `outputMock` mocks this process.
     |- `someSubProcessMock` mocks a sub process
     |""".stripMargin

val handledErrorsDescr =
  "A comma separated list of HTTP-Status-Codes, that are modelled in the BPMN as Business-Exceptions - see Outputs. z.B: `404,500`"
val regexHandledErrorsDescr =
  """If you specified _handledErrors_, you can specify Regexes that all must match the error messages.
Otherwise the error is thrown.
  
You can use a JSON Array of Strings or a comma-separated String.  

Example: `['java.sql.SQLException', '"errorNr":20000']` or 'java.sql.SQLException,"errorNr":20000'
"""
//TODO end move to bpmn
