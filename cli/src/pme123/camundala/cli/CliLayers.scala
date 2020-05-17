package pme123.camundala.cli

import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.ServicesLayers
import pme123.camundala.services.StandardApp.StandardAppDeps
import zio.ZLayer
import zio.clock.Clock
import zio.console.Console

object CliLayers {

  def cliLayer(appRunnerLayer: ZLayer[StandardAppDeps, Nothing, AppRunner]): ZLayer[StandardAppDeps, Throwable, CliApp] =
    (Clock.live ++
      Console.live ++
      CamundaLayers.bpmnServiceLayer ++
      ModelLayers.deployRegisterLayer ++
      CamundaLayers.deploymentServiceLayer ++
      CamundaLayers.httpDeployClientLayer ++
      ServicesLayers.dockerComposerLayer ++
      appRunnerLayer) >>> cliApp.live

}
