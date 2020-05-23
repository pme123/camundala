package pme123.camundala.camunda

import java.util.Date

import eu.timepit.refined.auto._
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity
import org.camunda.bpm.engine.repository.Deployment
import pme123.camundala.camunda.xml.MergeResult
import pme123.camundala.model.deploy.DeployId
import pme123.camundala.model.register.bpmnRegister
import zio._
import zio.test.Assertion.equalTo
import zio.test._

object DeploymentServiceSuite extends DefaultRunnableSpec {

  import TestData._

  private val deployment = {
    val depl = new DeploymentEntity()
    depl.setName("TwitterDemoProcess")
    depl.setId("TwitterDemoProcess")
    depl.setDeploymentTime(new Date())
    depl
  }

  def spec: ZSpec[environment.TestEnvironment, Any] = {
    suite("DeploymentServiceSuite")(
      testM("the BPMN Model can be deployed") {
        for {
          bpmnXml <- bpmnXmlTask
          deployRequest = DeployRequest("TwitterDemoProcess", deployFiles = Set(DeployFile("TwitterDemoProcess.bpmn", bpmnXml.toString.getBytes.toVector)))
          _ <- bpmnRegister.registerBpmn(bpmn)
          dr <- deploymentService.deploy(deployRequest)
        } yield
          assert(dr.id)(equalTo("TwitterDemoProcess"))
      }.provideCustomLayer(CamundaLayers.deploymentServiceLayer ++ bpmnRegister.live).mapError(TestFailure.fail)
    )
  }

  private val processEngineLayer = ZLayer.succeed(
    new processEngineService.Service {
      override def deploy(deployRequest: DeployRequest, mergeResults: Seq[MergeResult]): Task[Deployment] = Task(deployment)

      override def deployments(): Task[Seq[Deployment]] = Task(Seq(deployment))

      override def undeploy(deployId: DeployId): Task[Unit] = Task(())
    })
}