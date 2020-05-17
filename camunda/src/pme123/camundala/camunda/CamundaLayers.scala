package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.util.EngineUtil
import pme123.camundala.app.sttpBackend
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.deploymentService.DeploymentService
import pme123.camundala.camunda.httpDeployClient.HttpDeployClient
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.ModelLayers._
import zio.clock.Clock
import zio.{Has, TaskLayer, ZIO, ZLayer}

object CamundaLayers {

  lazy val bpmnServiceLayer: TaskLayer[BpmnService] = bpmnRegisterLayer >>> bpmnService.live
  lazy val httpDeployClientLayer: TaskLayer[HttpDeployClient] = sttpBackend.sttpBackendLayer ++ bpmnServiceLayer ++ ModelLayers.logLayer("DockerRunner") ++ Clock.live >>> httpDeployClient.live
  lazy val deploymentServiceLayer: TaskLayer[DeploymentService] = bpmnServiceLayer ++ processEngineServiceLayer >>> deploymentService.live

  lazy val processEngineLayer: ZLayer[Any, Throwable, Has[() => ProcessEngine]] = // ProcessEngine must be lazy!
    ZLayer.fromAcquireRelease(ZIO.effect(() => EngineUtil.lookupProcessEngine(null)))(pe =>
      ZIO.effect(pe().close()).ignore
    )
  lazy val processEngineServiceLayer: TaskLayer[ProcessEngineService] = processEngineLayer >>> processEngineService.live


}
