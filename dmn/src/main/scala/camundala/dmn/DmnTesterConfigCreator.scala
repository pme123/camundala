package camundala
package dmn

import camundala.bpmn.*
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import pme123.camunda.dmn.tester.shared.*

import java.time.LocalDateTime
import scala.language.{implicitConversions, reflectiveCalls}
import scala.reflect.{ClassTag, classTag}

trait DmnTesterConfigCreator extends DmnConfigWriter:

  // the path where the DMNs are
  protected def dmnBasePath: os.Path = starterConfig.dmnPaths.head
  // the path where the DMN Configs are
  protected def dmnConfigPath: os.Path = starterConfig.dmnConfigPaths.head
  // creating the Path to the DMN - by default the _dmnName_ is `decisionDmn.decisionDefinitionKey`.
  protected def defaultDmnPath(dmnName: String): os.Path = dmnBasePath / s"$dmnName.dmn"

  protected def createDmnConfigs(dmnTesterObjects: DmnTesterObject[?]*): Unit =
    println(s"createDmnConfigs: $dmnConfigPath")
    dmnConfigs(dmnTesterObjects)
      .foreach(updateConfig(_, dmnConfigPath))
    println("Check it on http://localhost:8883")

  given [In <: Product]: Conversion[DecisionDmn[In, _], DmnTesterObject[In]]
    with
    def apply(decisionDmn: DecisionDmn[In, _]): DmnTesterObject[In] =
      DmnTesterObject(
        decisionDmn,
        defaultDmnPath(decisionDmn.decisionDefinitionKey)
      )

  private def dmnConfigs(
      dmnTesterObjects: Seq[DmnTesterObject[?]]
  ): Seq[DmnConfig] =
    dmnTesterObjects
      .filterNot(_._inTestMode)
      .map { dmnTO =>
        val dmn = dmnTO.dDmn
        val in: Product = dmn.in
        val inputs = toInputs(in, dmnTO)
        val variables = toVariables(in)
        DmnConfig(
          dmn.decisionDefinitionKey,
          TesterData(inputs, variables),
          dmnTO.dmnPath.relativeTo(projectBasePath).segments.toList,
          testUnit = dmnTO._testUnit,
          acceptMissingRules = dmnTO._acceptMissingRules
        )

      }

  private def toInputs[T <: Product](
      product: T,
      dmnTO: DmnTesterObject[?]
  ) =
    product.productElementNames
      .zip(product.productIterator)
      .collect { case (k, v) if !v.isInstanceOf[DmnVariable[?]] =>
        testValues(k, v, dmnTO.addTestValues)
      }
      .toList

  private def testValues[E: ClassTag](
      k: String,
      value: E,
      addTestValues: Map[String, List[TesterValue]]
  ): TesterInput =
    val unwrapValue = value match
      case d: LocalDateTime => d.toString
      case Some(v) => v
      case v => v
    val isNullable = value match
      case Some(_) => true
      case _ => false
    //noinspection ScalaUnnecessaryParentheses
    unwrapValue match
      case v: (Double | Int | Long | Short | String | Float) =>
        TesterInput(
          k,
          isNullable,
          addTestValues.getOrElse(k, List(TesterValue.fromAny(v)))
        )
      case _: Boolean =>
        TesterInput(
          k,
          isNullable,
          List(TesterValue.fromAny(true), toTesterValue(false))
        )
      case v: scala.reflect.Enum =>
        val e: { def values: Array[?] } =
          v.asInstanceOf[{ def values: Array[?] }]
        TesterInput(
          k,
          isNullable,
          addTestValues.getOrElse(k, e.values.map(v => toTesterValue(v)).toList)
        )
      case v =>
        throw new IllegalArgumentException(
          s"Not supported for DMN Input ($k -> $v)"
        )

  private def toVariables[T <: Product](
      product: T,
  ) =
    product.productElementNames
      .zip(product.productIterator)
      .collect { case (k, v: DmnVariable[?]) =>
        val result: DmnValueType = v.value
        testValues(k, result, Map.empty)
      }
      .toList

  case class DmnTesterObject[In <: Product](
      dDmn: DecisionDmn[In, _],
      dmnPath: Path,
      addTestValues: Map[String, List[TesterValue]] = Map.empty,
      _testUnit: Boolean = false,
      _acceptMissingRules: Boolean = false,
      _inTestMode: Boolean = false
  )

  private def toTesterValue(value: Any) =
    value match
      // enums not supported in DmnTester 2.13
      case e: scala.reflect.Enum => TesterValue.fromAny(e.toString)
      case v => TesterValue.fromAny(v)

  extension [In <: Product](dmnTO: DmnTesterObject[In])

    def dmnPath(path: Path): DmnTesterObject[In] =
      dmnTO.copy(dmnPath = path)

    def dmnPath(dmnName: String): DmnTesterObject[In] =
      dmnPath(defaultDmnPath(dmnName))

    def testUnit: DmnTesterObject[In] =
      dmnTO.copy(_testUnit = true)

    def inTestMode: DmnTesterObject[In] =
      dmnTO.copy(_inTestMode = true)

    def acceptMissingRules: DmnTesterObject[In] =
      dmnTO.copy(_acceptMissingRules = true)

    inline def testValues(inline key: In => DmnValueType | Option[DmnValueType], values: Any*): DmnTesterObject[In] =
      val testerValues = values
        .map(v => toTesterValue(v.toString))
        .toList
      dmnTO.copy(addTestValues =
        dmnTO.addTestValues + (nameOfVariable(key) -> testerValues)
      )
end DmnTesterConfigCreator
