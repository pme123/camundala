package camundala
package bpmn

import camundala.domain.*
import io.circe
import io.circe.HCursor
import io.circe.syntax.*
import sttp.tapir.*
import sttp.tapir.SchemaType.SchemaWithValue

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import scala.reflect.ClassTag

case class Dmns(dmns: Seq[Dmn]):

  def :+(dmn: Dmn): Dmns = Dmns(dmns :+ dmn)

object Dmns:
  def none: Dmns = Dmns(Nil)

case class Dmn(path: Path, decisions: DecisionDmn[?, ?]*)

type DmnValueSimple = String | Boolean | Int | Long | Double | LocalDate |
  LocalDateTime | ZonedDateTime

type DmnValueType = DmnValueSimple | scala.reflect.Enum

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
      Activity[In, Out, DecisionDmn[In, Out]]:

  override val label: String =
    """// use singleEntry / collectEntries / singleResult / resultList
      |  dmn""".stripMargin
  lazy val decisionDefinitionKey: String = inOutDescr.id

  def withInOutDescr(descr: InOutDescr[In, Out]): DecisionDmn[In, Out] =
    copy(inOutDescr = descr)

end DecisionDmn

// String | Boolean | Int | Long | Double |
//  LocalDate | LocalDateTime | ZonedDateTime | scala.reflect.Enum
implicit def DmnValueTypeEncoder[T <: DmnValueSimple]: Encoder[T] =
  new Encoder[T] {
    final def apply(dv: T): Json = valueToJson(dv)
  }

implicit def LocalDateDecoder: Decoder[LocalDate] = new Decoder[LocalDate] {
  final def apply(c: HCursor): Decoder.Result[LocalDate] =
    for result <- c.as[String]
    yield LocalDate.parse(result)

}

implicit def LocalDateTimeDecoder: Decoder[LocalDateTime] =
  new Decoder[LocalDateTime] {
    final def apply(c: HCursor): Decoder.Result[LocalDateTime] =
      for result <- c.as[String]
      yield LocalDateTime.parse(result)

  }

implicit def ZonedDateTimeDecoder: Decoder[ZonedDateTime] =
  new Decoder[ZonedDateTime] {
    final def apply(c: HCursor): Decoder.Result[ZonedDateTime] =
      for result <- c.as[String]
      yield ZonedDateTime.parse(result)

  }

@description(
  "SingleEntry: Output of a DMN Table. This returns one `DmnValueType`."
)
case class SingleEntry[Out <: DmnValueType: Encoder: Decoder: ClassTag](
    result: Out
):
  lazy val toCamunda: CamundaVariable = CamundaVariable.valueToCamunda(result)
  val decisionResultType: DecisionResultType = DecisionResultType.singleEntry
object SingleEntry:
  implicit def schemaForSingleEntry[A <: DmnValueType : Encoder : Decoder](implicit
                                                                           sa: Schema[A]
                                                                          ): Schema[SingleEntry[A]] =
    Schema[SingleEntry[A]](
      SchemaType.SCoproduct(List(sa), None) { case SingleEntry(x) =>
        Some(SchemaWithValue(sa, x))
      },
      for {
        na <- sa.name
      } yield Schema.SName("SingleEntry", List(na.show))
    )

  implicit def SingleEntryCodec[T <: DmnValueType : CirceCodec : ClassTag]
  : CirceCodec[SingleEntry[T]] =
    CirceCodec.from(SingleEntryDecoder, SingleEntryEncoder)

  implicit def SingleEntryEncoder[T <: DmnValueType : Encoder]
  : Encoder[SingleEntry[T]] = new Encoder[SingleEntry[T]] {
    final def apply(sr: SingleEntry[T]): Json = sr.result.asJson
  }

  implicit def SingleEntryDecoder[T <: DmnValueType : Encoder : Decoder : ClassTag]
  : Decoder[SingleEntry[T]] = new Decoder[SingleEntry[T]] {
    final def apply(c: HCursor): Decoder.Result[SingleEntry[T]] =
      for result <- c.as[T]
        yield SingleEntry[T](result)

  }

end SingleEntry

@description(
  "CollectEntry: Output of a DMN Table. This returns a Sequence of `DmnValueType`s."
)
case class CollectEntries[Out <: DmnValueType : Encoder: Decoder: Schema](
    result: Seq[Out]
):
  lazy val toCamunda: Seq[CamundaVariable] =
    result.map(CamundaVariable.valueToCamunda)
  val decisionResultType: DecisionResultType = DecisionResultType.collectEntries

object CollectEntries:
  def apply[Out <: DmnValueType: Encoder: Decoder: Schema](
      result: Out,
      results: Out*
  ): CollectEntries[Out] =
    new CollectEntries[Out](result +: results)

  implicit def schemaForCollectEntries[A <: DmnValueType : Encoder : Decoder](
                                                                               implicit sa: Schema[A]
                                                                             ): Schema[CollectEntries[A]] =
    Schema[CollectEntries[A]](
      SchemaType.SCoproduct(List(sa), None) { case CollectEntries(x) =>
        x.headOption.map(SchemaWithValue(sa, _))
      },
      for {
        na <- sa.name
      } yield Schema.SName("CollectEntries", List(na.show))
    )

  implicit def CollectEntriesEncoder[T <: DmnValueType : Encoder : Decoder]
  : Encoder[CollectEntries[T]] = new Encoder[CollectEntries[T]] {
    final def apply(sr: CollectEntries[T]): Json = sr.result.asJson
  }

  implicit def CollectEntriesDecoder[T <: DmnValueType : Encoder : Decoder : Schema]
  : Decoder[CollectEntries[T]] = new Decoder[CollectEntries[T]] {
    final def apply(c: HCursor): Decoder.Result[CollectEntries[T]] =
      for result <- c.as[Seq[T]]
        yield CollectEntries[T](result)

  }
end CollectEntries

@description(
  "SingleResult: Output of a DMN Table. This returns one `Product` (case class) with more than one fields of `DmnValueType`s."
)
case class SingleResult[Out <: Product: Encoder: Decoder: Schema](result: Out):

  lazy val toCamunda: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(result)
  val decisionResultType: DecisionResultType = DecisionResultType.singleResult
object SingleResult:
  implicit def schemaForSingleResult[A <: Product : Encoder : Decoder](implicit
                                                                       sa: Schema[A]
                                                                      ): Schema[SingleResult[A]] =
    Schema[SingleResult[A]](
      SchemaType.SCoproduct(List(sa), None) { case SingleResult(x) =>
        Some(SchemaWithValue(sa, x))
      },
      for {
        na <- sa.name
      } yield Schema.SName("SingleResult", List(na.show))
    )

  implicit def SingleResultEncoder[T <: Product : Encoder : Decoder : Schema]
  : Encoder[SingleResult[T]] = new Encoder[SingleResult[T]] {
    final def apply(sr: SingleResult[T]): Json = sr.result.asJson
  }

  implicit def SingleResultDecoder[T <: Product : Encoder : Decoder : Schema]
  : Decoder[SingleResult[T]] = new Decoder[SingleResult[T]] {
    final def apply(c: HCursor): Decoder.Result[SingleResult[T]] =
      for result <- c.as[T]
        yield SingleResult[T](result)

  }
end SingleResult

@description(
  "ResultList: Output of a DMN Table. This returns a Sequence of `Product`s (case classes) with more than one fields of `DmnValueType`s"
)
case class ResultList[Out <: Product: Encoder: Decoder: Schema](
    result: Seq[Out]
):

  lazy val toCamunda: Seq[Map[String, CamundaVariable]] =
    result.map(CamundaVariable.toCamunda)
  val decisionResultType: DecisionResultType = DecisionResultType.resultList

object ResultList:
  def apply[Out <: Product: Encoder: Decoder: Schema](
      result: Out,
      results: Out*
  ): ResultList[Out] =
    new ResultList[Out](result +: results)

  implicit def schemaForResultList[A <: Product: Encoder: Decoder](implicit
      sa: Schema[A]
  ): Schema[ResultList[A]] =
    Schema[ResultList[A]](
      SchemaType.SCoproduct(List(sa), None) { case ResultList(x) =>
        x.headOption.map(SchemaWithValue(sa, _))
      },
      for {
        na <- sa.name
      } yield Schema.SName("ResultList", List(na.show))
    )

  implicit def ResultListEncoder[T <: Product: Encoder: Decoder: Schema]
      : Encoder[ResultList[T]] = new Encoder[ResultList[T]] {
    final def apply(sr: ResultList[T]): Json = sr.result.asJson
  }
  implicit def ResultListDecoder[T <: Product: Encoder: Decoder: Schema]
      : Decoder[ResultList[T]] = new Decoder[ResultList[T]] {
    final def apply(c: HCursor): Decoder.Result[ResultList[T]] =
      for result <- c.as[Seq[T]]
      yield ResultList[T](result)

  }
end ResultList

object DecisionDmn:

  def init(id: String): DecisionDmn[NoInput, SingleEntry[String]] =
    DecisionDmn(
      InOutDescr(id, NoInput(), SingleEntry("INIT ONLY"))
    )

@description(
  "A wrapper, to indicate if an Input is a Variable."
)
case class DmnVariable[In <: DmnValueType: ClassTag](
    value: In
)


implicit def schemaForDmnVariable[A <: DmnValueType](implicit
                                                                   sa: Schema[A]
                                                                  ): Schema[DmnVariable[A]] =
  Schema[DmnVariable[A]](
    SchemaType.SCoproduct(List(sa), None) { case DmnVariable(x) =>
      Some(SchemaWithValue(sa, x))
    },
    for {
      na <- sa.name
    } yield Schema.SName("DmnVariable", List(na.show))
  )

implicit def DmnVariableEncoder[T <: DmnValueType: Encoder: ClassTag]
: Encoder[DmnVariable[T]] = new Encoder[DmnVariable[T]] {
  final def apply(sr: DmnVariable[T]): Json = sr.value.asJson
}
implicit def DmnVariableDecoder[T <: DmnValueType: Decoder: ClassTag]
: Decoder[DmnVariable[T]] = new Decoder[DmnVariable[T]] {
  final def apply(c: HCursor): Decoder.Result[DmnVariable[T]] =
    for value <- c.as[T]
      yield DmnVariable[T](value)

}


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
