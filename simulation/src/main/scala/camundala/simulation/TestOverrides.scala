package camundala.simulation

import camundala.api.CamundaProperty
import camundala.bpmn.*
import camundala.bpmn.CamundaVariable.*

case class TestOverride(
    key: String,
    overrideType: TestOverrideType, // problem with encoding?! derives JsonTaggedAdt.PureEncoder
    value: Option[CamundaVariable] = None
)

case class TestOverrides(overrides: Seq[TestOverride]): //Seq[TestOverride])

  def :+(testOverride: TestOverride): TestOverrides = TestOverrides(overrides :+ testOverride)

enum TestOverrideType derives Adt.PureEncoder:
  case Exists, NotExists, IsEquals, HasSize

object TestOverrideType:
  given Schema[TestOverrideType] = Schema.derived

def addOverride[
    T <: Product
](
    model: T,
    key: String,
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
  given Schema[TestOverrides] = Schema.derived

  given Encoder[TestOverrides] = deriveEncoder

  given Decoder[TestOverrides] = deriveDecoder

object TestOverride:
  given Schema[TestOverride] = Schema.derived

  given Encoder[TestOverride] = deriveEncoder

  given Decoder[TestOverride] = deriveDecoder

trait TestOverrideExtensions:

  extension [T <: WithTestOverrides[T]] (withOverride: T)

    def exists(
                key: String
              ): T =
      add(key, TestOverrideType.Exists)

    def notExists(
                   key: String
                 ): T =
      add(key, TestOverrideType.NotExists)

    def isEquals(
                  key: String,
                  value: Any
                ): T =
      add(
        key,
        TestOverrideType.IsEquals,
        Some(CamundaVariable.valueToCamunda(value))
      )

    def hasSize(
                 key: String,
                 size: Int
               ): T =
      add(
        key,
        TestOverrideType.HasSize,
        Some(CInteger(size))
      )

    private def add(
                         key: String,
                         overrideType: TestOverrideType,
                         value: Option[CamundaVariable] = None
                       ): T =
      withOverride.add(TestOverride(key, overrideType, value))
