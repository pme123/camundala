package camundala
package test

import bpmn.*
import io.circe.*
import io.circe.generic.auto
import io.circe.syntax.*
import org.camunda.bpm.engine.variable.Variables.fileValue
import org.camunda.spin.Spin
import org.camunda.spin.json.SpinJsonNode
import org.camunda.spin.plugin.variable.SpinValues
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl

import scala.annotation.tailrec
import scala.jdk.CollectionConverters.*

case class TestConfig(
                       deploymentResources: Set[ResourcePath] = Set.empty,
                       serviceRegistries: Set[ServiceRegistry] = Set.empty,
                     )

case class ServiceRegistry(key: String, value: Any)
case class MockedCallActivity(id: String, javaVars: java.util.Map[String, Any])

case class BpmnTestCases(
                          testConfig: TestConfig = TestConfig(),
                          testCases: List[BpmnTestCase] = List.empty
                        )

case class BpmnTestCase(processes: List[ProcessToTest[?, ?]] = List.empty)

case class ProcessToTest[
  In <: Product : Encoder : Decoder,
  Out <: Product : Encoder : Decoder
](
   process: Process[In, Out],
   steps: List[ElementToTest] = List.empty
 )

sealed trait ElementToTest

case class NodeToTest(
                       inOut: ProcessNode,
                       in: Map[String, Any] = Map.empty,
                       out: Map[String, Any] = Map.empty
                     ) extends ElementToTest

case class CustomTests(tests: () => Unit) extends ElementToTest

extension[T <: Product : Encoder] (product: T)
  def names(): Seq[String] = product.productElementNames.toSeq

  def asVars(): Map[String, Any] =
    names()
      .zip(product.productIterator)
      .toMap

  def asValueMap(): Map[String, Any] =
    asVars()
      .filterNot { case k -> v =>
        v.isInstanceOf[None.type]
      } // don't send null
      .map { case (k, v) =>
        k -> objectToVM(k, v)
      }

  def objectToVM(
                  key: String,
                  value: Any
                ): Any =
    value match
      case Some(v) => objectToVM(key, v)
      case c: Iterable[?] =>
        val f = c.map(objectToVM(key, _))
        valueToVM(key, f)
      case v =>
        valueToVM(key, v)

  def valueToVM(key: String, value: Any): Any =
    println(s"valueToVM($key: String, $value: Any)")
    value match
      case v: scala.reflect.Enum =>
        v.toString
      case FileInOut(fileName, content, mimeType) =>
        fileValue(fileName)
          .file(content)
          .mimeType(mimeType.orNull)
          .create
      case v: Product =>
        println(s"PRODUCT: $key :: ${product}")

        product.asJson.deepDropNullValues.hcursor
          .downField(key)
          .as[Json] match
          case Right(v) =>
            println(s"JSON: ${v}")
            val jsonValue = Spin.JSON(
              v.toString
            ) //new JsonValueImpl(Spin.JSON(v.toString), null, null, true)
            println(s"SPIN: ${jsonValue}")
            jsonValue
          case Left(ex) =>
            throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
      case _ => value

  def asJavaVars(): java.util.Map[String, Any] =
    asValueMap().asJava

  def asDmnVars(): Map[String, Any] =
    asVars()
      .map {
        case (k, v: scala.reflect.Enum) =>
          (k, v.toString)
        case (k, v) => (k, v)
      }

end extension
