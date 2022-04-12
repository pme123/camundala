package camundala
package domain

import bpmn.*

import java.util.Base64
import io.circe.{ACursor, Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*

case class NoInput()
object NoInput:
  given Schema[NoInput] = Schema.derived
  given Encoder[NoInput] = deriveEncoder
  given Decoder[NoInput] = deriveDecoder

case class NoOutput()
object NoOutput :
  given Schema[NoOutput] = Schema.derived
  given Encoder[NoOutput] = deriveEncoder
  given Decoder[NoOutput] = deriveDecoder


case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

object FileInOut :
  given Schema[FileInOut] = Schema.derived
  given Encoder[FileInOut] = deriveEncoder
  given Decoder[FileInOut] = deriveDecoder

def valueToJson(value: Any): Json =
  value match
    case v: Int =>
      Json.fromInt(v)
    case v: Long =>
      Json.fromLong(v)
    case v: Boolean =>
      Json.fromBoolean(v)
    case v: Float =>
      Json.fromFloat(v).getOrElse(Json.Null)
    case v: Double =>
      Json.fromDouble(v).getOrElse(Json.Null)
    case null =>
      Json.Null
    case v =>
      Json.fromString(v.toString)
