package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.util.EngineUtil
import pme123.camundala.app.sttpBackend
import pme123.camundala.camunda.bpmnGenerator.BpmnGenerator
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.camundaProcessEngine.CamundaProcessEngine
import pme123.camundala.camunda.httpDeployClient.HttpDeployClient
import pme123.camundala.camunda.scenarioRunner.ScenarioRunner
import pme123.camundala.camunda.service.restService
import pme123.camundala.camunda.service.restService.RestService
import pme123.camundala.camunda.userManagement.UserManagement
import pme123.camundala.config.ConfigLayers
import pme123.camundala.model.ModelLayers
import zio._
import zio.clock.Clock
import zio.logging.Logging
import zio.random.Random

object CamundaLayers {

  def logLayer(loggerName: String): ULayer[Logging] =
    ModelLayers.logLayer(loggerName, "pme123.camundala.camunda")

  lazy val restServicetLayer: TaskLayer[RestService] = sttpBackend.sttpBackendLayer ++ logLayer("RestService") ++ Clock.live >>> restService.live

  lazy val bpmnServiceLayer: TaskLayer[BpmnService] = ConfigLayers.appConfigLayer ++ ModelLayers.bpmnRegisterLayer >>> bpmnService.live
  lazy val httpDeployClientLayer: TaskLayer[HttpDeployClient] =
    bpmnServiceLayer ++ logLayer("DockerRunner") ++ restServicetLayer ++ ConfigLayers.appConfigLayer >>> httpDeployClient.live
  lazy val bpmnGeneratorLayer: TaskLayer[BpmnGenerator] = logLayer("BpmnGenerator") ++ ConfigLayers.appConfigLayer >>> bpmnGenerator.live

  lazy val processEngineLayer: TaskLayer[Has[() => ProcessEngine]] = // ProcessEngine must be lazy!
    ZLayer.fromAcquireRelease(ZIO.effect(() => EngineUtil.lookupProcessEngine(null)))(pe =>
      ZIO.effect(pe().close()).ignore
    )

  lazy val camundaProcessEngineLayer: TaskLayer[CamundaProcessEngine] = processEngineLayer >>> camundaProcessEngine.live

  lazy val userManagementLayer: TaskLayer[UserManagement] = camundaProcessEngineLayer ++ logLayer("UserManagement") >>> userManagement.live

  lazy val scenarioRunnerLayer: TaskLayer[ScenarioRunner] = camundaProcessEngineLayer ++ Random.live ++ logLayer("ScenarioRunner") >>> scenarioRunner.live

}
