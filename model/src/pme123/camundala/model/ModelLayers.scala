package pme123.camundala.model

import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.model.register.{bpmnRegister, deployRegister}
import zio.ULayer
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging

object ModelLayers {
  def logLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )
  lazy val bpmnRegisterLayer: ULayer[BpmnRegister] = bpmnRegister.live
  lazy val deployRegisterLayer: ULayer[DeployRegister] = deployRegister.live

}
