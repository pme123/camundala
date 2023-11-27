package camundala
package simulation

import bpmn.*
import bpmn.CamundaVariable.*
import domain.*


case class TestOverride(
    key: Option[String],
    overrideType: TestOverrideType,
    value: Option[CamundaVariable] = None
)

case class TestOverrides(overrides: Seq[TestOverride]): //Seq[TestOverride])

  def :+(testOverride: TestOverride): TestOverrides = TestOverrides(
    overrides :+ testOverride
  )

enum TestOverrideType:
  case Exists, NotExists, IsEquals, HasSize, Contains

object TestOverrideType:
  given JsonCodec[TestOverrideType] = deriveCodec
  given ApiSchema[TestOverrideType] = deriveSchema

def addOverride[
    T <: Product
](
    model: T,
    key: Option[String],
    overrideType: TestOverrideType,
    value: Option[CamundaVariable] = None
): TestOverrides =
  val testOverride = TestOverride(key, overrideType, value)
  val newOverrides: Seq[TestOverride] = model match
    case TestOverrides(overrides) =>
      overrides :+ testOverride
    case _ =>
      Seq(testOverride)
  TestOverrides(newOverrides)

object TestOverrides:
  given ApiSchema[TestOverrides] = deriveSchema

  given JsonEncoder[TestOverrides] = deriveEncoder

  given JsonDecoder[TestOverrides] = deriveDecoder

object TestOverride:
  given ApiSchema[TestOverride] = deriveSchema

  given JsonEncoder[TestOverride] = deriveEncoder

  given JsonDecoder[TestOverride] = deriveDecoder

trait TestOverrideExtensions:

  extension [T <: WithTestOverrides[T]](withOverride: T)

    def exists(
        key: String
    ): T =
      add(Some(key), TestOverrideType.Exists)

    def notExists(
        key: String
    ): T =
      add(Some(key), TestOverrideType.NotExists)

    def isEquals[V: JsonEncoder](
        key: String,
        value: V
    ): T =
      add(
        Some(key),
        TestOverrideType.IsEquals,
        camundaVariable(value)
      )
    // used for collections
    def hasSize(
        key: String,
        size: Int
    ): T =
      add(
        Some(key),
        TestOverrideType.HasSize,
        Some(CInteger(size))
      )

    // used for DMNs ResultList and CollectEntries
    def hasSize(
        size: Int
    ): T =
      add(
        None,
        TestOverrideType.HasSize,
        Some(CInteger(size))
      )

    // used for collections
    def contains[V: JsonEncoder](
                              key: String,
                              value: V
                            ): T =
      add(
        Some(key),
        TestOverrideType.Contains,
        camundaVariable(value)
      )

    // used for DMNs ResultList and CollectEntries
    def contains[V: JsonEncoder](
        expected: V
    ): T =
      add(
        None,
        TestOverrideType.Contains,
        Some(CamundaVariable.valueToCamunda(expected.asJson))
      )

    private def add(
        key: Option[String],
        overrideType: TestOverrideType,
        value: Option[CamundaVariable] = None
    ): T =
      withOverride.add(TestOverride(key, overrideType, value))


    private def camundaVariable[V: JsonEncoder](
                                             value: V
                                           ) =
      val v = value match
        case _: scala.reflect.Enum => value
        case _: (Seq[_] | Product) => value.asJson
        case _ => value
      Some(CamundaVariable.valueToCamunda(v))
