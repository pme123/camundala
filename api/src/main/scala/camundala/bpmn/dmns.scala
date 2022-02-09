package camundala
package bpmn

import camundala.domain.*
import io.circe.HCursor
import io.circe.syntax.*
import org.joda.time.LocalTime
import sttp.tapir.*
import sttp.tapir.SchemaType.{SProduct, SProductField}

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import java.util.Date

case class Dmns(dmns: Seq[Dmn]):

  def :+(dmn: Dmn): Dmns = Dmns(dmns :+ dmn)

object Dmns:
  def none: Dmns = Dmns(Nil)

case class Dmn(path: Path, decisions: DecisionDmn[?, ?]*)

type DmnValueType = String | Boolean | Int | Long | Double | Date |
  LocalDateTime | ZonedDateTime | scala.reflect.Enum

enum DecisionResultType:
  case singleEntry // TypedValue
  case singleResult // Map(String, Object)
  case collectEntries // List(Object)
  case resultList // List(Map(String, Object))

case class DecisionDmn[
    In <: Product: Encoder: Decoder: Schema,
    Out <: Product: Encoder: Decoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode,
      InOut[In, Out, DecisionDmn[In, Out]]:

  override val label =
    """// use singleEntry / collectEntries / singleResult / resultList
      |  dmn""".stripMargin
  lazy val decisionDefinitionKey: String = inOutDescr.id

  def withInOutDescr(descr: InOutDescr[In, Out]): DecisionDmn[In, Out] =
    copy(inOutDescr = descr)

  def decisionResultType: DecisionResultType = {
    (inOutDescr.out) match
      case o: Product if o.isSingleEntry =>
        DecisionResultType.singleEntry
      case o: Product if o.isCollectEntries =>
        DecisionResultType.collectEntries
      case o: Product if o.isSingleResult =>
        DecisionResultType.singleResult
      case o: Product if o.isResultList =>
        DecisionResultType.resultList
  }

case class SingleResult[Out <: Product: Encoder: Decoder: Schema](result: Out)

implicit def schemaForSingleResult[A <: Product: Encoder: Decoder](implicit
    sa: Schema[A]
): Schema[SingleResult[A]] =
  Schema[SingleResult[A]](
    SchemaType.SCoproduct(List(sa), None) { case SingleResult(_) =>
      Some(sa)
    },
    for {
      na <- sa.name
    } yield Schema.SName("SingleResult", List(na.show))
  )

implicit def SingleResultEncoder[T <: Product: Encoder: Decoder: Schema]
    : Encoder[SingleResult[T]] = new Encoder[SingleResult[T]] {
  final def apply(sr: SingleResult[T]): Json = Json.obj(
    ("result", sr.asJson)
  )
}
implicit def SingleResultDecoder[T <: Product: Encoder: Decoder: Schema]
    : Decoder[SingleResult[T]] = new Decoder[SingleResult[T]] {
  final def apply(c: HCursor): Decoder.Result[SingleResult[T]] =
    for result <- c.downField("result").as[T]
    yield SingleResult[T](result)

}

case class ResultList[Out <: Product: Encoder: Decoder: Schema](result: Seq[Out])

implicit def schemaForResultList[A <: Product: Encoder: Decoder](implicit
                                                                   sa: Schema[A]
                                                                  ): Schema[ResultList[A]] =
  Schema[ResultList[A]](
    SchemaType.SCoproduct(List(sa), None) { case ResultList(_) =>
      Some(sa)
    },
    for {
      na <- sa.name
    } yield Schema.SName("ResultList", List(na.show))
  )

implicit def ResultListEncoder[T <: Product: Encoder: Decoder: Schema]
: Encoder[ResultList[T]] = new Encoder[ResultList[T]] {
  final def apply(sr: ResultList[T]): Json = Json.obj(
    ("result", sr.asJson)
  )
}
implicit def ResultListDecoder[T <: Product: Encoder: Decoder: Schema]
: Decoder[ResultList[T]] = new Decoder[ResultList[T]] {
  final def apply(c: HCursor): Decoder.Result[ResultList[T]] =
    for result <- c.downField("result").as[Seq[T]]
      yield ResultList[T](result)

}

object DecisionDmn:

  def init(id: String): DecisionDmn[NoInput, NoOutput] =
    DecisionDmn(
      InOutDescr(id, NoInput(), NoOutput())
    )

extension (output: Product)

  def isSingleEntry =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case _: DmnValueType => true
        case _ => false
      )

  def isSingleResult =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case _: Iterable[?] => false
        case p: Product =>
          p.productIterator.size > 1 &&
            p.productIterator.forall(_.isInstanceOf[DmnValueType])
        case _ => false
      )

  def isCollectEntries: Boolean =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case p: Iterable[?] =>
          p.headOption match
            case Some(p: DmnValueType) => true
            case o => false
        case o => false
      )

  def isResultList =
    output.productIterator.size == 1 &&
      (output.productIterator.next() match
        case p: Iterable[?] =>
          p.headOption match
            case Some(p: Product) =>
              p.productIterator.size > 1 &&
                p.productIterator.forall(_.isInstanceOf[DmnValueType])
            case o => false
        case o => false
      )
  def hasManyOutputVars: Boolean =
    isSingleResult || isResultList
end extension // Product

