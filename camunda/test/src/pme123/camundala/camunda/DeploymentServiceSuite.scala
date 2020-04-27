package pme123.camundala.camunda

import pme123.camundala.camunda.deploymentService.{DeployFile, DeployRequest}
import zio.test.Assertion.equalTo
import zio.test._

import scala.collection.immutable.HashSet
import scala.io.Source
import scala.xml.XML

object DeploymentServiceSuite {/* extends DefaultRunnableSpec {

  private val bpmn = Source.fromResource("bpmn/TwitterModelProcess.bpmn")
  private val deployRequest = DeployRequest(deployFiles = HashSet(DeployFile("myTask.bpmn", bpmn.reader().)))


  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is correct") {
        deploymentService.deploy().registerProcess(deployRequest) *>
        assertM( deploymentService.deploy())(
          equalTo(expected))
      }
    ).provideCustomLayer(((processRegister.live >>> bpmnService.live) ++ processRegister.live).mapError(TestFailure.fail))
*/}