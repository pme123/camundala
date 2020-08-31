package pme123.camundala.cli

import pme123.camundala.services.appRunner.AppRunner
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.ServicesLayers
import pme123.camundala.services.StandardApp.StandardAppDeps
import zio.{ULayer, ZLayer}
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object CliLayers {

  def logLayer(loggerName: String, prefix: String = "pme123.camundala.cli"): ULayer[Logging] =
    Slf4jLogger.make((_, message) =>
      message,
      Some(s"$prefix.$loggerName")
    )
  def cliLayer(appRunnerLayer: ZLayer[StandardAppDeps, Nothing, AppRunner]): ZLayer[StandardAppDeps, Throwable, CliApp] =
    (Clock.live ++
      Console.live ++
      CamundaLayers.bpmnServiceLayer ++
      ModelLayers.deployRegisterLayer ++
      CamundaLayers.httpDeployClientLayer ++
      CamundaLayers.bpmnGeneratorLayer ++
      CamundaLayers.userManagementLayer ++
      CamundaLayers.scenarioRunnerLayer ++
      ServicesLayers.dockerComposerLayer ++
      appRunnerLayer ++
      logLayer("CliApp")) >>> cliApp.live

}
