package pme123.camundala.services

import pme123.camundala.app.sttpBackend
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.config.ConfigLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.StandardApp.StandardAppDeps
import pme123.camundala.services.dockerComposer.DockerComposer
import pme123.camundala.services.httpServer.HttpServer
import zio.logging.Logging
import zio.{TaskLayer, ULayer}

object ServicesLayers {

  private def logLayer(loggerName: String): ULayer[Logging] =
    ModelLayers.logLayer(loggerName, "pme123.camundala.services")

  lazy val httpServerLayer: TaskLayer[HttpServer] =
    logLayer("httpServer") ++
      ConfigLayers.appConfigLayer ++
      CamundaLayers.deploymentServiceLayer >>> httpServer.live

  lazy val appDepsLayer: TaskLayer[StandardAppDeps] =
    logLayer("appLayer") ++
      httpServerLayer ++
      ConfigLayers.appConfigLayer ++
      CamundaLayers.bpmnServiceLayer ++
      ModelLayers.bpmnRegisterLayer ++
      ModelLayers.deployRegisterLayer

  lazy val dockerComposerLayer: TaskLayer[DockerComposer] =
    sttpBackend.sttpBackendLayer ++ ModelLayers.logLayer("DockerRunner") >>> dockerComposer.live
}
