package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.deploymentService.DeploymentService
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmnRegister.BpmnRegister
import pme123.camundala.model.{Bpmn, bpmnRegister}
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging

trait ZSpringApp extends zio.App {

  protected def registerBpmns(bpmns: Set[Bpmn]): URIO[BpmnRegister, List[Unit]] =
    ZIO.foreach(bpmns.toSeq)(b => bpmnRegister.registerBpmn(b))

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  protected def managedSpringApp(clazz: Class[_], args: List[String]): ZManaged[Console, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      console.putStrLn("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      console.putStrLn("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll(ex =>
          console.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
        )
    )

  protected def logLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )

  protected lazy val bpmnRegisterLayer: ULayer[BpmnRegister] = bpmnRegister.live
  protected lazy val bpmnServiceLayer: TaskLayer[BpmnService] = bpmnRegisterLayer >>> bpmnService.live
  protected lazy val appConfigLayer: TaskLayer[AppConfig] = logLayer("appConfig") >>> appConfig.live

  protected lazy val processEngineLayer: ZLayer[Any, Throwable, Has[() => ProcessEngine]] = // ProcessEngine must be lazy!
    ZLayer.fromAcquireRelease(ZIO.effect(() => EngineUtil.lookupProcessEngine(null)))(pe =>
      ZIO.effect(pe().close()).ignore
    )
  protected lazy val processEngineServiceLayer: TaskLayer[ProcessEngineService] = processEngineLayer >>> processEngineService.live

  protected lazy val deploymentServiceLayer: TaskLayer[DeploymentService] = bpmnServiceLayer ++ processEngineServiceLayer >>> deploymentService.live

}
