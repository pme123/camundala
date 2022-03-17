package camundala
package domain

import bpmn.*

import java.util.Base64
import io.circe.{ACursor, Decoder, Encoder, HCursor, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.*
import org.camunda.bpm.engine.variable.Variables.fileValue

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*

case class NoInput()
object NoInput:
  implicit lazy val NoInputSchema: Schema[NoInput] = Schema.derived
  implicit lazy val NoInputEncoder: Encoder[NoInput] = deriveEncoder
  implicit lazy val NoInputDecoder: Decoder[NoInput] = deriveDecoder

case class NoOutput()
object NoOutput :
  implicit lazy val NoOutputSchema: Schema[NoOutput] = Schema.derived
  implicit lazy val NoOutputEncoder: Encoder[NoOutput] = deriveEncoder
  implicit lazy val NoOutputDecoder: Decoder[NoOutput] = deriveDecoder


case class FileInOut(
    fileName: String,
    @description("The content of the File as a Byte Array.")
    content: Array[Byte],
    mimeType: Option[String]
):
  lazy val contentAsBase64: String = Base64.getEncoder.encodeToString(content)

object FileInOut :
  implicit lazy val FileInOutSchema: Schema[FileInOut] = Schema.derived
  implicit lazy val FileInOutEncoder: Encoder[FileInOut] = deriveEncoder
  implicit lazy val FileInOutDecoder: Decoder[FileInOut] = deriveDecoder

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
