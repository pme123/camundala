package camundala
package domain

import camundala.api.FileInOut
import io.circe.{ACursor, Decoder, Encoder, HCursor, Json}
import sttp.tapir.{Schema, SchemaType}
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.camunda.bpm.engine.variable.Variables.fileValue
import sttp.tapir.generic.Derived
import sttp.tapir.generic.auto.*

import scala.jdk.CollectionConverters.*

case class NoInput()
case class NoOutput()

extension (product: Product)
  def names(): Seq[String] = product.productElementNames.toSeq

  def asVars(): Map[String, Any] =
    product.productElementNames
      .zip(product.productIterator)
      .toMap

  def asVarsWithoutEnums(): Map[String, Any] =
      asVars()
        .filter(_._2 match
          case None => false
          case _ => true
        )
        .map {
          case key -> Some(v) => key -> v
          case key -> v => key -> v
        }
        .map {
        case (k, FileInOut(fileName, content, mimeType)) =>
          k -> fileValue(fileName).file(content).mimeType(mimeType.orNull).create
        case (k, e: scala.reflect.Enum) =>
          k -> e.toString
        case (k, it: Seq[?]) =>
          k -> it.map {
            case e: scala.reflect.Enum => e.toString
            case e: AnyVal => e
            case o => o.toString
          }.asJava
        case other =>
          other
      }

  def asJavaVars(): java.util.Map[String, Any] =
    asVarsWithoutEnums().asJava

  def asDmnVars(): Map[String, Any] =
    asVars()
      .map {
        case (k, v: scala.reflect.Enum) =>
          (k, v.toString)
        case (k, v) => (k, v)
      }

end extension
/*
case class ManyInOut[
    T <: Product: Encoder: Decoder: Schema
](inOut: T, examples: T*):
  def toSeq: Seq[T] = inOut +: examples

object ManyInOut:
  def apply[
      T <: Product: Encoder: Decoder: Schema
  ](inOuts: Seq[T]): ManyInOut[T] =
    ManyInOut(inOuts.head, inOuts.tail: _*)

implicit def encodeManyInOut[
    T <: Product: Encoder: Decoder: Schema
]: Encoder[ManyInOut[T]] = new Encoder[ManyInOut[T]] {
  final def apply(a: ManyInOut[T]): Json =
    Json.arr(
      (a.inOut.asJson +: a.examples.map(_.asJson)): _*
    ) //Seq(a.inOut, a.examples).map(_.asJson))
}

implicit def decodeManyInOut[
    T <: Product: Encoder: Decoder: Schema
]: Decoder[ManyInOut[T]] = new Decoder[ManyInOut[T]] {
  final def apply(c: HCursor): Decoder.Result[ManyInOut[T]] =
    for {
      arr <- c.as[Seq[T]]
    } yield {
      ManyInOut(arr)
    }
}

implicit def schemaForNel[T <: Product: Encoder: Decoder: Schema]
    : Schema[ManyInOut[T]] =
  Schema[ManyInOut[T]](SchemaType.SArray(implicitly[Schema[T]])(_.toSeq))
*/

def valueToJson(value: Any): Json =
  value match
    case v: Int =>
      v.asJson
    case v: Long =>
      v.asJson
    case v: Boolean =>
      v.asJson
    case v: Float =>
      v.asJson
    case v: Double =>
      v.asJson
    case v =>
      v.toString.asJson
