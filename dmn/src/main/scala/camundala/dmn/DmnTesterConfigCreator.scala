package camundala
package dmn

import camundala.bpmn.*
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.latestbit.circe.adt.codec.JsonTaggedAdt
import pme123.camunda.dmn.tester.shared.*

import scala.language.{implicitConversions, reflectiveCalls}
import scala.reflect.{ClassTag, classTag}

trait DmnTesterConfigCreator extends DmnConfigWriter:

  protected def dmnBasePath: os.Path = starterConfig.dmnPaths.head
  protected def dmnConfigPath: os.Path = starterConfig.dmnConfigPaths.head
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
    dmnTesterObjects.map { dmnTO =>
      val dmn = dmnTO.dDmn
      val in: Product = dmn.in
      val testerData = toConfig(in, dmnTO.addTestValues)
      DmnConfig(
        dmn.decisionDefinitionKey,
        TesterData(testerData.toList),
        dmnTO.dmnPath.relativeTo(projectBasePath).segments.toList,
        testUnit = dmnTO._testUnit,
        acceptMissingRules = dmnTO._acceptMissingRules
      )

    }

  private def toConfig[T <: Product](
      product: T,
      addTestValues: Map[String, List[TesterValue]]
  ) =
    product.productElementNames
      .zip(product.productIterator)
      .map { case (k, v) => testValues(k, v, addTestValues) }

  private def testValues[E: ClassTag](
      k: String,
      value: E,
      addTestValues: Map[String, List[TesterValue]]
  ) =
    val unwrapValue = value match
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
          e.values.map(v => toTesterValue(v)).toList
        )
      case v =>
        throw new IllegalArgumentException(
          s"Not supported for DMN Input ($k -> $v)"
        )

  case class DmnTesterObject[In <: Product](
      dDmn: DecisionDmn[In, _],
      dmnPath: Path,
      addTestValues: Map[String, List[TesterValue]] = Map.empty,
      _testUnit: Boolean = false,
      _acceptMissingRules: Boolean = false
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

    def acceptMissingRules: DmnTesterObject[In] =
      dmnTO.copy(_acceptMissingRules = true)

    def testValues(key: String, values: Any*): DmnTesterObject[In] =
      dmnTO.copy(addTestValues =
        dmnTO.addTestValues + (key -> values
          .map(v => toTesterValue(v.toString))
          .toList)
      )

end DmnTesterConfigCreator
