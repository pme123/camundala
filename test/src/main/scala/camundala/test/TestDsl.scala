package camundala
package test

import bpmn.*
import io.circe.{Decoder, Encoder}

import scala.annotation.tailrec
import scala.language.implicitConversions
import scala.jdk.CollectionConverters.*


val baseResource: ResourcePath = os.resource
def formResource: ResourcePath = os.resource / "static" / "forms"

trait TestDsl:

  def testConfig: TestConfig =
    TestConfig()
  
  extension (testConfig: TestConfig)

    def deployments(deployments: ResourcePath*): TestConfig =
      testConfig.copy(deploymentResources = deployments.toSet)
    def registries(sRegistries: ServiceRegistry*): TestConfig =
      testConfig.copy(serviceRegistries = sRegistries.toSet)

  end extension

  def serviceRegistry(key: String, value: Any): ServiceRegistry = ServiceRegistry(key, value)

  def custom(tests: => Unit): CustomTests = CustomTests(() => tests)


  def test[
    In <: Product: Encoder: Decoder,
    Out <: Product: Encoder: Decoder
  ](process: Process[In, Out])(
    activities: ElementToTest*
  ): Unit
  
  implicit def toTest[
    In <: Product: Encoder: Decoder,
    Out <: Product: Encoder: Decoder
  ](inOut: InOut[In,Out, ?] & ProcessNode): NodeToTest =
    NodeToTest(inOut, inOut.in.asValueMap(), inOut.out.asValueMap())

  implicit def toTest(pn: EndEvent): NodeToTest =
    NodeToTest(pn)

