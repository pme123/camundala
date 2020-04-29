package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.util.EngineUtil
import pme123.camundala.camunda.deploymentService.{DeployFile, DeployRequest}
import pme123.camundala.model.bpmnRegister
import zio.test.Assertion.equalTo
import zio.test._

import scala.collection.immutable.HashSet
import scala.io.Source
import scala.xml.XML

object DeploymentServiceSuite extends DefaultRunnableSpec {

  import TestData._
  private lazy val processEngine: ProcessEngine = EngineUtil.lookupProcessEngine(null)

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is correct") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          dr <- deploymentService.deploy(DeployRequest(Some("TwitterDemoProcess"), deployFiles = Set(DeployFile("TwitterDemoProcess.bpmn", XML.load(bpmnResource.reader()).toString().getBytes().toVector))))
        } yield
          assert(dr.id)(equalTo("TwitterDemoProcess"))
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live >>> deploymentService.live(processEngine)) ++ bpmnRegister.live).mapError(TestFailure.fail))
}