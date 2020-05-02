package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.util.EngineUtil
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.deploymentService.DeploymentService
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn.bpmnRegister
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.deploy.deployRegister
import pme123.camundala.model.deploy.deployRegister.DeployRegister
import zio.{Has, TaskLayer, ULayer, ZIO, ZLayer}
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging

object DefaultLayers {
  def logLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )

  lazy val bpmnRegisterLayer: ULayer[BpmnRegister] = bpmnRegister.live
  lazy val deployRegisterLayer: ULayer[DeployRegister] = deployRegister.live
  lazy val bpmnServiceLayer: TaskLayer[BpmnService] = bpmnRegisterLayer >>> bpmnService.live
  lazy val appConfigLayer: TaskLayer[AppConfig] = logLayer("appConfig") >>> appConfig.live

  lazy val processEngineLayer: ZLayer[Any, Throwable, Has[() => ProcessEngine]] = // ProcessEngine must be lazy!
    ZLayer.fromAcquireRelease(ZIO.effect(() => EngineUtil.lookupProcessEngine(null)))(pe =>
      ZIO.effect(pe().close()).ignore
    )
  lazy val processEngineServiceLayer: TaskLayer[ProcessEngineService] = processEngineLayer >>> processEngineService.live

  lazy val deploymentServiceLayer: TaskLayer[DeploymentService] = bpmnServiceLayer ++ processEngineServiceLayer >>> deploymentService.live

}
