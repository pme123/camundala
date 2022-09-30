package camundala
package test

import bpmn.*
import io.circe.Json
import org.assertj.core.api.{Condition, MapAssert}
import org.assertj.core.data.MapEntry
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.spin.Spin
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.util
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
export org.camunda.bpm.engine.runtime.ProcessInstance as CProcessInstance

trait CommonTesting extends TestDsl:

  // context function def f(using CProcessInstance): Unit
  type FromProcessInstance[Unit] = CProcessInstance ?=> Unit

  def config: TestConfig

  @Rule
  def processEngineRule = new ProcessEngineRule

  @Before
  def init(): Unit =
    deployment()
    setUpRegistries()

  def deployment(): Unit =
    val deployment = repositoryService.createDeployment()
    val resources = config.deploymentResources
    println(s"Resources: $resources")
    resources.foreach(r =>
      deployment.addInputStream(
        r.toString,
        new java.io.ByteArrayInputStream(os.read.bytes(r))
      )
    )
    deployment.deploy()

  def setUpRegistries(): Unit =
    MockitoAnnotations.initMocks(this)
    val serviceRegistries = config.serviceRegistries
    println(s"ServiceRegistries: $serviceRegistries")
    serviceRegistries.foreach { case ServiceRegistry(key, value) =>
      Mocks.register(key, value)
    }

  def setUpMockedCallActivities(): Unit = ()

  def checkOutput(out: Map[String, Any]): FromProcessInstance[Unit] =
    val assertion = assertThat(summon[CProcessInstance])
    val variables = assertion.variables()
    //println(s"CHECK: ${variables.extractingByKeys("success").is(Condition())}")
    def checkValue(key: String, value: Any): Condition[util.Map[String, Any]] =
      Condition(
        me => {
          println(
            s"ME ${me.get(key)} ${me.get(key).getClass} - $value ${value.getClass}"
          )
          value match
            case jn: JacksonJsonNode =>
              val cJson = toJson(jn.toString)
              val pJson = toJson(
                me.get(key) match
                  case jn2: JacksonJsonNode =>
                    me.get(key).toString
                  case other: util.Collection[?] =>
                    other.asScala.toSeq
                      .map {
                        case a: String =>
                          s"\"$a\""
                        case o =>
                          o.toString
                      }
                      .mkString("[", ", ", "]")
              )
              val setCJson = cJson.as[Set[Json]].toOption.getOrElse(cJson)
              val setPJson = pJson.as[Set[Json]].toOption.getOrElse(pJson)
              println(
                s"ME2 ${setCJson} ${setCJson.getClass} - $setPJson ${setPJson.getClass}"
              )

              setCJson == setPJson
            case _ =>
              me.get(key) == value
        },
        s"Check variable $key has $value (${value.getClass})"
      )
    for
      (k, v) <- out
      _ = assertion.hasVariables(k)
    yield
      println(s"CHECK: ${v.getClass}")
      variables.has(checkValue(k, v)) //containsEntry(k, v)

end CommonTesting
