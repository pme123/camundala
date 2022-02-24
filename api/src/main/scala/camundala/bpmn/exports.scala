package camundala
package bpmn

import io.circe.{Json, parser}
import io.circe.syntax.*
import org.latestbit.circe.adt.codec.impl

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple}

val camundaVersion = "7.15"

// os
export os.{pwd, Path, ResourcePath, read}

// sttp
export sttp.model.StatusCode

// circe
export io.circe.{Decoder, Encoder, Json}
export io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

// One import for this ADT/JSON codec
export org.latestbit.circe.adt.codec.JsonTaggedAdt
export org.latestbit.circe.adt.codec.{JsonTaggedAdt => Adt}

// tapir
export sttp.tapir.EndpointIO.Example
export sttp.tapir.EndpointOutput.Void
export sttp.tapir.Endpoint
export sttp.tapir.PublicEndpoint
export sttp.tapir.endpoint
export sttp.tapir.EndpointOutput
export sttp.tapir.EndpointInput
export sttp.tapir.oneOf
export sttp.tapir.oneOfVariant
export sttp.tapir.path
export sttp.tapir.query
export sttp.tapir.Schema
export sttp.tapir.stringToPath
export sttp.tapir.Schema.annotations.description

def throwErr(err: String) =
  println(s"ERROR: $err")
  throw new IllegalArgumentException(err)

def toJson(json: String): Json =
  parser.parse(json) match
    case Right(v) => v.deepDropNullValues
    case Left(exc) =>
      throwErr(s"Could not create Json from your String -> $exc")

def toJsonString[T <: Product: Encoder](product: T): String =
  product.asJson.deepDropNullValues.toString

