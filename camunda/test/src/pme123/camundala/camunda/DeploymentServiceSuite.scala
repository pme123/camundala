package pme123.camundala.camunda

import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity
import org.camunda.bpm.engine.repository.Deployment
import pme123.camundala.camunda.deploymentService.{DeployFile, DeployRequest}
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.camunda.xml.MergeResult
import pme123.camundala.model.bpmnRegister
import zio.{Has, Task, ULayer, URLayer, ZLayer}
import zio.test.Assertion.equalTo
import zio.test._
import zio.test.junit.JUnitRunnableSpec
import zio.test.mock.Expectation._
import zio.test.mock._

import scala.xml.XML

object DeploymentServiceSuite extends JUnitRunnableSpec {

  import TestData._

  private val deployRequest = DeployRequest(Some("TwitterDemoProcess"), deployFiles = Set(DeployFile("TwitterDemoProcess.bpmn", XML.load(bpmnResource.reader()).toString().getBytes().toVector)))
  private val deployment = {
    val depl = new DeploymentEntity()
    depl.setName(deployRequest.name.get)
    depl
  }

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("DeploymentServiceSuite")(
      testM("the BPMN Model can be deployed") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          dr <- deploymentService.deploy(deployRequest)
        } yield
          assert(dr.id)(equalTo("TwitterDemoProcess"))
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live ++ processEngineMockEnv >>> deploymentService.live) ++ bpmnRegister.live).mapError(TestFailure.fail))

  val processEngineMockEnv: ULayer[ProcessEngineService] = {
    ProcessEngineMock.Deploy(equalTo(deployRequest, Seq.empty[MergeResult])) returns value(deployment)
  }

//  @mockable[processEngineService.Service]
//  object ProcessEngineMock


  object ProcessEngineMock {

    sealed trait Tag[I, A] extends mock.Method[ProcessEngineService, I, A] {
      def envBuilder: URLayer[Has[mock.Proxy], ProcessEngineService] =
        ProcessEngineMock.envBuilder
    }

    object Deploy extends Tag[(DeployRequest, Seq[MergeResult]), Deployment]

    private val envBuilder: URLayer[Has[mock.Proxy], ProcessEngineService] =
      ZLayer.fromService(invoke =>
        new processEngineService.Service {
           def deploy(deployRequest: DeployRequest, mergeResults: Seq[MergeResult]): Task[Deployment] =
             invoke(Deploy, deployRequest, mergeResults)

        }
      )
  }
}