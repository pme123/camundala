package camundala
package dmn

import bpmn.*
import domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.latestbit.circe.adt.codec.JsonTaggedAdt
import pme123.camunda.dmn.tester.shared.*

import scala.language.{implicitConversions, reflectiveCalls}
import scala.reflect.{ClassTag, classTag}

trait DmnTesterConfigCreator extends DmnConfigWriter:

  private def dmnBasePath: os.Path = starterConfig.dmnPaths.head
  private def dmnConfigPath: os.Path = starterConfig.dmnConfigPaths.head
  def defaultDmnPath(dmnKey: String): os.Path = dmnBasePath / s"$dmnKey.dmn"

  def dmnTester(dmnTesterObjects: DmnTesterObject*): Seq[Unit] =
    dmnConfigs(dmnTesterObjects).map { c =>
      updateConfig(c, dmnConfigPath)
    }

  implicit def toTesterObjectScenario(
                                   decisionDmn: DecisionDmn[_, _]
                                ): DmnTesterObject =
    DmnTesterObject(decisionDmn, defaultDmnPath(decisionDmn.decisionDefinitionKey))

  private def dmnConfigs(
      dmnTesterObjects: Seq[DmnTesterObject]
  ): Seq[DmnConfig] =
    dmnTesterObjects.map { dmnTO =>
      val dmn = dmnTO.dDmn
      val in: Product = dmn.in
      val testerData = toConfig(in, dmnTO.addTestValues)
      DmnConfig(
        dmn.decisionDefinitionKey,
        TesterData(testerData.toList),
        dmnTO.dmnPath.relativeTo(projectBasePath).segments.toList,
        testUnit = false
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

    //noinspection ScalaUnnecessaryParentheses
    unwrapValue match
      case v: (Double | Int | Long | Short | String | Float) =>
        TesterInput(
          k,
          false,
          addTestValues.getOrElse(k, List(TesterValue.fromAny(v)))
        )
      case _: Boolean =>
        TesterInput(
          k,
          false,
          List(TesterValue.fromAny(true), toTesterValue(false))
        )
      case v: scala.reflect.Enum =>
        val e: { def values: Array[?] } =
          v.asInstanceOf[{ def values: Array[?] }]
        TesterInput(
          k,
          false,
          e.values.map(v => toTesterValue(v)).toList
        )
      case v =>
        throw new IllegalArgumentException(
          s"Not supported for DMN Input ($k -> $v)"
        )

  case class DmnTesterObject(
      dDmn: DecisionDmn[_, _],
      dmnPath: Path,
      addTestValues: Map[String, List[TesterValue]] = Map.empty
  )

  private def toTesterValue(value: Any) =
    value match
      // enums not supported in DmnTester 2.13
      case e: scala.reflect.Enum => TesterValue.fromAny(e.toString)
      case v => TesterValue.fromAny(v)

  extension (dmnTO: DmnTesterObject)
    def dmnPath(path: Path): DmnTesterObject =
      dmnTO.copy(dmnPath = path)

    def testValues(key: String, values: AnyVal*): DmnTesterObject =
      dmnTO.copy(addTestValues =
        dmnTO.addTestValues + (key -> values
          .map(v => toTesterValue(v.toString))
          .toList)
      )

end DmnTesterConfigCreator
