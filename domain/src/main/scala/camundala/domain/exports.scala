package camundala
package domain

import java.util.Base64

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

