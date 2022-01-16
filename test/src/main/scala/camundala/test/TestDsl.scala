package camundala
package test

import camundala.bpmn.{Process, ProcessNode}
import os.{Path, ResourcePath, pwd}

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
  val baseResource: ResourcePath = os.resource
  def formResource: ResourcePath = os.resource / "static" / "forms"

  def custom(tests: => Unit): CustomTests = CustomTests(() => tests)
