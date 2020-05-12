package pme123.camundala.services

import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.dockerComposer.DockerComposer
import zio.ZLayer

object ServicesLayers {
  val dockerComposerLayer: ZLayer[Any, Throwable, DockerComposer] =
    sttpBackend.sttpBackendLayer ++ ModelLayers.logLayer("DockerRunner") >>> dockerComposer.live
}
