package camundala
package bpmn

import camundala.domain.*
import io.circe
import io.circe.*
import sttp.tapir.*
import sttp.tapir.SchemaType.SchemaWithValue

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}
import scala.reflect.ClassTag

case class Dmns(dmns: Seq[Dmn]):

  def :+(dmn: Dmn): Dmns = Dmns(dmns :+ dmn)

object Dmns:
  def none: Dmns = Dmns(Nil)

case class Dmn(path: os.Path, decisions: DecisionDmn[?, ?]*)

type DmnValueSimple = String | Boolean | Int | Long | Double | LocalDate |
  LocalDateTime | ZonedDateTime

type DmnValueType = DmnValueSimple | scala.reflect.Enum

enum DecisionResultType:
  case singleEntry // TypedValue
  case singleResult // Map(String, Object)
  case collectEntries // List(Object)
  case resultList // List(Map(String, Object))
end DecisionResultType

case class DecisionDmn[
    In <: Product: JsonEncoder: JsonDecoder: ApiSchema,
    Out <: Product: JsonEncoder: JsonDecoder: ApiSchema
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
given DmnValueTypeJsonEncoder[T <: DmnValueSimple]: JsonEncoder[T] =
  new JsonEncoder[T]:
    final def apply(dv: T): Json = valueToJson(dv)

given LocalDateJsonDecoder: JsonDecoder[LocalDate] = new JsonDecoder[LocalDate]:
  final def apply(c: HCursor): Decoder.Result[LocalDate] =
    for result <- c.as[String]
    yield LocalDate.parse(result)

given LocalDateTimeJsonDecoder: JsonDecoder[LocalDateTime] =
  new JsonDecoder[LocalDateTime]:
    final def apply(c: HCursor): Decoder.Result[LocalDateTime] =
      for result <- c.as[String]
      yield LocalDateTime.parse(result)

given JsonDecoder[ZonedDateTime] =
  new JsonDecoder[ZonedDateTime]:
    final def apply(c: HCursor): Decoder.Result[ZonedDateTime] =
      for result <- c.as[String]
      yield ZonedDateTime.parse(result)

@description(
  "SingleEntry: Output of a DMN Table. This returns one `DmnValueType`."
)
case class SingleEntry[Out <: DmnValueType: JsonEncoder: JsonDecoder: ClassTag](
    result: Out
):
  lazy val toCamunda: CamundaVariable = CamundaVariable.valueToCamunda(result)
  val decisionResultType: DecisionResultType = DecisionResultType.singleEntry
end SingleEntry

object SingleEntry:

  given schemaForSingleEntry[A <: DmnValueType: JsonEncoder: JsonDecoder: ApiSchema]: ApiSchema[SingleEntry[A]] =
    val sa = summon[Schema[A]]
    Schema[SingleEntry[A]](
      SchemaType.SCoproduct(List(sa), None) { case SingleEntry(x) =>
        Some(SchemaWithValue(sa, x))
      },
      for
        na <- sa.name
      yield Schema.SName("SingleEntry", List(na.show))
    )
  end schemaForSingleEntry

  given SingleEntryCodec[T <: DmnValueType: JsonCodec: ClassTag]: JsonCodec[SingleEntry[T]] =
    CirceCodec.from(SingleEntryJsonDecoder, SingleEntryJsonEncoder)

  given SingleEntryJsonEncoder[T <: DmnValueType: JsonEncoder]: JsonEncoder[SingleEntry[T]] =
    new JsonEncoder[SingleEntry[T]]:
      final def apply(sr: SingleEntry[T]): Json = sr.result.asJson

  given SingleEntryJsonDecoder[T <: DmnValueType: JsonEncoder: JsonDecoder: ClassTag]: JsonDecoder[SingleEntry[T]] =
    new JsonDecoder[SingleEntry[T]]:
      final def apply(c: HCursor): Decoder.Result[SingleEntry[T]] =
        for result <- c.as[T]
        yield SingleEntry[T](result)
end SingleEntry

@description(
  "CollectEntry: Output of a DMN Table. This returns a Sequence of `DmnValueType`s."
)
case class CollectEntries[Out <: DmnValueType: JsonEncoder: JsonDecoder: ApiSchema](
    result: Seq[Out]
):
  lazy val toCamunda: Seq[CamundaVariable] =
    result.map(CamundaVariable.valueToCamunda)
  val decisionResultType: DecisionResultType = DecisionResultType.collectEntries
end CollectEntries

object CollectEntries:
  def apply[Out <: DmnValueType: JsonEncoder: JsonDecoder: ApiSchema](
      result: Out,
      results: Out*
  ): CollectEntries[Out] =
    new CollectEntries[Out](result +: results)

  given schemaForCollectEntries[A <: DmnValueType: JsonEncoder: JsonDecoder: ApiSchema]
      : ApiSchema[CollectEntries[A]] =
    val sa = summon[Schema[A]]
    Schema[CollectEntries[A]](
      SchemaType.SCoproduct(List(sa), None) { case CollectEntries(x) =>
        x.headOption.map(SchemaWithValue(sa, _))
      },
      for
        na <- sa.name
      yield Schema.SName("CollectEntries", List(na.show))
    )
  end schemaForCollectEntries

  given CollectEntriesJsonEncoder[T <: DmnValueType: JsonEncoder: JsonDecoder]: JsonEncoder[CollectEntries[T]] =
    new JsonEncoder[CollectEntries[T]]:
      final def apply(sr: CollectEntries[T]): Json = sr.result.asJson

  given CollectEntriesJsonDecoder[T <: DmnValueType: JsonEncoder: JsonDecoder: ApiSchema]
      : JsonDecoder[CollectEntries[T]] = new JsonDecoder[CollectEntries[T]]:
    final def apply(c: HCursor): Decoder.Result[CollectEntries[T]] =
      for result <- c.as[Seq[T]]
      yield CollectEntries[T](result)
end CollectEntries

@description(
  "SingleResult: Output of a DMN Table. This returns one `Product` (case class) with more than one fields of `DmnValueType`s."
)
case class SingleResult[Out <: Product: JsonEncoder: JsonDecoder: ApiSchema](result: Out):

  lazy val toCamunda: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(result)
  val decisionResultType: DecisionResultType = DecisionResultType.singleResult
end SingleResult

object SingleResult:
  given schemaForSingleResult[A <: Product: JsonEncoder: JsonDecoder: ApiSchema]: ApiSchema[SingleResult[A]] =
    val sa = summon[Schema[A]]
    Schema[SingleResult[A]](
      SchemaType.SCoproduct(List(sa), None) { case SingleResult(x) =>
        Some(SchemaWithValue(sa, x))
      },
      for
        na <- sa.name
      yield Schema.SName("SingleResult", List(na.show))
    )
  end schemaForSingleResult

  given SingleResultJsonEncoder[T <: Product: JsonEncoder: JsonDecoder: ApiSchema]: JsonEncoder[SingleResult[T]] =
    new JsonEncoder[SingleResult[T]]:
      final def apply(sr: SingleResult[T]): Json = sr.result.asJson

  given SingleResultJsonDecoder[T <: Product: JsonEncoder: JsonDecoder: ApiSchema]: JsonDecoder[SingleResult[T]] =
    new JsonDecoder[SingleResult[T]]:
      final def apply(c: HCursor): Decoder.Result[SingleResult[T]] =
        for result <- c.as[T]
        yield SingleResult[T](result)
end SingleResult

@description(
  "ResultList: Output of a DMN Table. This returns a Sequence of `Product`s (case classes) with more than one fields of `DmnValueType`s"
)
case class ResultList[Out <: Product: JsonEncoder: JsonDecoder: ApiSchema](
    result: Seq[Out]
):

  lazy val toCamunda: Seq[Map[String, CamundaVariable]] =
    result.map(CamundaVariable.toCamunda)
  val decisionResultType: DecisionResultType = DecisionResultType.resultList
end ResultList

object ResultList:
  def apply[Out <: Product: JsonEncoder: JsonDecoder: ApiSchema](
      result: Out,
      results: Out*
  ): ResultList[Out] =
    new ResultList[Out](result +: results)

  given schemaForResultList[A <: Product: JsonEncoder: JsonDecoder: ApiSchema]: ApiSchema[ResultList[A]] =
    val sa = summon[Schema[A]]
    Schema[ResultList[A]](
      SchemaType.SCoproduct(List(sa), None) { case ResultList(x) =>
        x.headOption.map(SchemaWithValue(sa, _))
      },
      for
        na <- sa.name
      yield Schema.SName("ResultList", List(na.show))
    )
  end schemaForResultList

  given ResultListEncoder[T <: Product: JsonEncoder: JsonDecoder: ApiSchema]: JsonEncoder[ResultList[T]] =
    new Encoder[ResultList[T]]:
      final def apply(sr: ResultList[T]): Json = sr.result.asJson

  given ResultListDecoder[T <: Product: JsonEncoder: JsonDecoder: ApiSchema]: JsonDecoder[ResultList[T]] =
    new Decoder[ResultList[T]]:
      final def apply(c: HCursor): Decoder.Result[ResultList[T]] =
        for result <- c.as[Seq[T]]
        yield ResultList[T](result)
end ResultList

object DecisionDmn:

  def init(id: String): DecisionDmn[NoInput, SingleEntry[String]] =
    DecisionDmn(
      InOutDescr(id, NoInput(), SingleEntry("INIT ONLY"))
    )
end DecisionDmn

@description(
  "A wrapper, to indicate if an Input is a Variable."
)
case class DmnVariable[In <: DmnValueType: ClassTag](
    value: In
)
object DmnVariable:
  given schemaForDmnVariable[A <: DmnValueType: ApiSchema]: ApiSchema[DmnVariable[A]] =
    val sa = summon[Schema[A]]
    Schema[DmnVariable[A]](
      SchemaType.SCoproduct(List(sa), None) { case DmnVariable(x) =>
        Some(SchemaWithValue(sa, x))
      },
      for
        na <- sa.name
      yield Schema.SName("DmnVariable", List(na.show))
    )
  end schemaForDmnVariable

  given DmnVariableEncoder[T <: DmnValueType: JsonEncoder: ClassTag]: JsonEncoder[DmnVariable[T]] =
    new JsonEncoder[DmnVariable[T]]:
      final def apply(sr: DmnVariable[T]): Json = sr.value.asJson
  given DmnVariableDecoder[T <: DmnValueType: JsonDecoder: ClassTag]: JsonDecoder[DmnVariable[T]] =
    new JsonDecoder[DmnVariable[T]]:
      final def apply(c: HCursor): Decoder.Result[DmnVariable[T]] =
        for value <- c.as[T]
        yield DmnVariable[T](value)
end DmnVariable

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
