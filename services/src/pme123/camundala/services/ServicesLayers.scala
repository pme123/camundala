package pme123.camundala.services

import pme123.camundala.app.sttpBackend
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.config.ConfigLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.StandardApp.StandardAppDeps
import pme123.camundala.services.dockerComposer.DockerComposer
import pme123.camundala.services.httpServer.HttpServer
import zio.TaskLayer

object ServicesLayers {

  lazy val httpServerLayer: TaskLayer[HttpServer] =
    ModelLayers.logLayer("httpServer") ++
      ConfigLayers.appConfigLayer ++
      CamundaLayers.deploymentServiceLayer >>> httpServer.live

  lazy val appDepsLayer: TaskLayer[StandardAppDeps] =
    ModelLayers.logLayer("appLayer") ++
      httpServerLayer ++
      CamundaLayers.bpmnServiceLayer ++
      ModelLayers.bpmnRegisterLayer ++
      ModelLayers.deployRegisterLayer

  lazy val dockerComposerLayer: TaskLayer[DockerComposer] =
    sttpBackend.sttpBackendLayer ++ ModelLayers.logLayer("DockerRunner") >>> dockerComposer.live
}
