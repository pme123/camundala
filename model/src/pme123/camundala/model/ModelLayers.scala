package pme123.camundala.model

import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.model.register.{bpmnRegister, deployRegister}
import zio.ULayer
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object ModelLayers {

   def logLayer(loggerName: String, prefix: String = "pme123.camundala.model"): ULayer[Logging] =
    Slf4jLogger.make((context, message) =>
      message,
      Some(s"$prefix.$loggerName")
    )

  lazy val bpmnRegisterLayer: ULayer[BpmnRegister] = bpmnRegister.live
  lazy val deployRegisterLayer: ULayer[DeployRegister] = deployRegister.live

}
