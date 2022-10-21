package camundala
package bpmn

import domain.*

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

