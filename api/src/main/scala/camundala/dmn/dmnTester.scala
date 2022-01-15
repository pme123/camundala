package camundala
package dmn

case class DmnConfig(
    decisionId: String,
    data: TesterData,
    dmnPath: List[String] = List.empty,
    isActive: Boolean = false
) 

case class TesterData(
    inputs: List[TesterInput],
    // simple input-, output-variables used in the DMN
    variables: List[TesterInput] = List.empty,
)

case class TesterInput(
    key: String,
    nullValue: Boolean,
    values: List[String]
)

sealed trait TesterValue {
  def valueStr: String

  def valueType: String

  def value: Any
}

object TesterValue {

  def fromAny(value: Any): TesterValue =
    value match {
      case b: Boolean => BooleanValue(b)
      case n:Long => NumberValue(n)
      case n:Double => NumberValue(n)
      case s: String if s == NullValue.constant => NullValue
      case s: String => StringValue(s)
      case o if o == null => NullValue
      case o => throw new IllegalArgumentException(s"Not expected value type: $o")
    }

  def valueMap(inputs: Map[String, Any]): Map[String, TesterValue] =
    inputs.view.mapValues(fromAny).toMap

  case class StringValue(value: String) extends TesterValue {
    val valueStr: String = value
    val valueType: String = "String"
  }

  case class BooleanValue(value: Boolean) extends TesterValue {
    val valueStr: String = value.toString
    val valueType: String = "Boolean"
  }

  object BooleanValue {
    def apply(strValue: String): BooleanValue =
      BooleanValue(strValue == "true")
  }

  case class NumberValue(value: BigDecimal) extends TesterValue {
    val valueStr: String = value.toString()
    val valueType: String = "Number"
  }

  object NumberValue {
    def apply(strValue: String): NumberValue =
      NumberValue(BigDecimal(strValue))

    def apply(intValue: Int): NumberValue =
      NumberValue(BigDecimal(intValue))

    def apply(longValue: Long): NumberValue =
      NumberValue(BigDecimal(longValue))

    def apply(doubleValue: Double): NumberValue =
      NumberValue(BigDecimal(doubleValue))

  }

  case object NullValue extends TesterValue {
    val valueStr: String = "null"
    val valueType: String = "Null"
    val constant: String = "_NULL_"

    val value: Any = null
  }
}


object conversions {

  implicit def stringToTesterValue(x: String): TesterValue =
    TesterValue.StringValue(x)

  implicit def intToTesterValue(x: Int): TesterValue =
    TesterValue.NumberValue(BigDecimal(x))

  implicit def longToTesterValue(x: Long): TesterValue =
    TesterValue.NumberValue(BigDecimal(x))

  implicit def doubleToTesterValue(x: Double): TesterValue =
    TesterValue.NumberValue(BigDecimal(x))

  implicit def booleanToTesterValue(x: Boolean): TesterValue =
    TesterValue.BooleanValue(x)
}
