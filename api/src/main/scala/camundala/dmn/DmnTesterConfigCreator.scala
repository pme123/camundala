package camundala
package dmn

import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.latestbit.circe.adt.codec.JsonTaggedAdt

import scala.language.reflectiveCalls
import scala.reflect.{ClassTag, classTag}

trait DmnTesterConfigCreator extends App:

  def basePath: Path = pwd / "dmnTester" / "dmnConfigs"
  def dmnBasePath: Path = pwd / "dmnTester" / "dmns"
  def defaultDmnPath(dmnKey: String) = dmnBasePath / s"$dmnKey.dmn"

  def dmnTester(dmnTesterObjects: DmnTesterObject*) =
    dmnConfigs(dmnTesterObjects).map { c =>
      val path = basePath / s"${c.decisionId}.conf"
      if (os.exists(path))
        os.remove(path)
      os.write(path, c.asJson.toString, createFolders = true)
      println(s"Created Open API $path")
    }

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
        dmnTO.dmnPath.relativeTo(pwd).segments.toList
      )

    }

  import reflect.Selectable.reflectiveSelectable

  private def toConfig[T <: Product](
      product: T,
      addTestValues: Map[String, List[String]]
  ) =
    product.productElementNames
      .zip(product.productIterator)
      .map { case (k, v) => testValues(k, v, addTestValues) }

  private def testValues[E: ClassTag](
      k: String,
      value: E,
      addTestValues: Map[String, List[String]]
  ) =
    val unwrapValue = value match
      case Some(v) => v
      case v => v

    unwrapValue match
      case v: (Double | Int | Long | Short | String | Float) =>
        TesterInput(
          k,
          false,
          addTestValues.getOrElse(k, List(v.toString))
        )
      case v: Boolean =>
        TesterInput(k, false, List("true", "false"))
      case v: scala.reflect.Enum =>
        val e: { def values: Array[?] } = v.asInstanceOf[{ def values: Array[?] }]
        TesterInput(
          k,
          false,
          e.values.map(_.toString).toList
        )
      case v =>
        throw new IllegalArgumentException(
          s"Not supported for DMN Input ($k -> $v)"
        )

  case class DmnTesterObject(
      dDmn: DecisionDmn[_, _],
      dmnPath: Path,
      addTestValues: Map[String, List[String]] = Map.empty
  )

  extension (dDmn: DecisionDmn[_, _])
    def tester: DmnTesterObject =
      DmnTesterObject(dDmn, defaultDmnPath(dDmn.decisionDefinitionKey))

  extension (dmnTO: DmnTesterObject)
    def dmnPath(path: Path) =
      dmnTO.copy(dmnPath = path)

    def testValues(key: String, values: AnyVal*) =
      dmnTO.copy(addTestValues =
        dmnTO.addTestValues + (key -> values.map(_.toString).toList)
      )

end DmnTesterConfigCreator
