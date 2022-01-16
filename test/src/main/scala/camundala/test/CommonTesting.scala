package camundala
package test

import camundala.bpmn.*
import camundala.domain.*
import org.assertj.core.api.MapAssert
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
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

  def test[
    In <: Product,
    Out <: Product
  ](process: Process[In, Out])(
    activities: (ProcessNode | CustomTests)*
  ): Unit

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

  def checkOutput[T <: Product](out: T): FromProcessInstance[Unit] =
    val assertion = assertThat(summon[CProcessInstance])
    val variables = assertion.variables()
    for
      (k, v) <- out.asVarsWithoutEnums()
      _ = assertion.hasVariables(k)
    yield
      variables.containsEntry(k, v)

end CommonTesting
