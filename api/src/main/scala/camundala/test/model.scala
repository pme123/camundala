package camundala
package test

import os.{Path, ResourcePath}
import bpmn.*

case class TestConfig(
    deploymentResources: Set[ResourcePath] = Set.empty,
    serviceRegistries: Set[ServiceRegistry] = Set.empty
)

case class ServiceRegistry(key: String, value: Any)

case class BpmnTestCases(
    testConfig: TestConfig = TestConfig(),
    testCases: List[BpmnTestCase] = List.empty
)

case class BpmnTestCase(processes: List[ProcessToTest[?, ?]] = List.empty)

case class ProcessToTest[
    In <: Product,
    Out <: Product
](process: Process[In, Out], steps: List[ProcessNode | CustomTests] = List.empty)

case class CustomTests(tests: () => Unit)
