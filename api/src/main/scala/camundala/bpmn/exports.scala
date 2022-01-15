package camundala
package bpmn

import io.circe.{Json, parser}
import io.circe.syntax.*

import scala.deriving.Mirror
import scala.compiletime.{constValue, constValueTuple}

val camundaVersion = "7.15"

// os
export os.{pwd, Path, read}

// sttp
export sttp.model.StatusCode

// circe
export io.circe.{Decoder, Encoder, Json}
// One import for this ADT/JSON codec
export org.latestbit.circe.adt.codec.JsonTaggedAdt

// tapir
export sttp.tapir.EndpointIO.Example
export sttp.tapir.EndpointOutput.Void
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

/** <pre> Only works if you have it as a constant, like:
  *
  * val invoiceCategoryDescr: String = enumDescr[InvoiceCategory]("There are
  * three possible Categories")
  *
  * @description(invoiceCategoryDescr)
  *   invoiceCategory: InvoiceCategory = InvoiceCategory.`Travel Expenses`
  */
inline def enumDescr[E](
    descr: String
)(using m: Mirror.SumOf[E]): String =
  enumDescr(Some(descr))

inline def enumDescr[E](
    descr: Option[String] = None
)(using m: Mirror.SumOf[E]): String =
  val name = constValue[m.MirroredLabel]
  val values =
    constValueTuple[m.MirroredElemLabels].productIterator.mkString(", ")
  val enumDescription =
    s"Enumeration $name: \n- $values"
  descr
    .map(_ + s"\n\n$enumDescription")
    .getOrElse(enumDescription)
