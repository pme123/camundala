package camundala
package domain

import io.circe.{Json, parser}
import java.util.Base64
import scala.language.implicitConversions

// circe
export io.circe.{Decoder, Encoder, Json}
export io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// One import for this ADT/JSON codec
export org.latestbit.circe.adt.codec.JsonTaggedAdt
export org.latestbit.circe.adt.codec.{JsonTaggedAdt => Adt}

// tapir
export sttp.tapir.Schema
export sttp.tapir.Schema.annotations.description


case class FileInOut(
                      fileName: String,
                      @description("The content of the File as a Byte Array.")
                      content: Array[Byte],
                      mimeType: Option[String]
                    ):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

object FileInOut:
  given Schema[FileInOut] = Schema.derived
  given Encoder[FileInOut] = deriveEncoder
  given Decoder[FileInOut] = deriveDecoder

/**
 * In Camunda 8 only json is allowed!
 */
case class FileRefInOut(
                         fileName: String,
                         @description("A reference to retrieve the file in your application.")
                         ref: String,
                         mimeType: Option[String]
                       )

object FileRefInOut:
  given Schema[FileRefInOut] = Schema.derived
  given Encoder[FileRefInOut] = deriveEncoder
  given Decoder[FileRefInOut] = deriveDecoder


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
