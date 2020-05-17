package pme123.camundala.config

import pme123.camundala.config.appConfig.AppConfig
import zio.{TaskLayer, ULayer}
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger

object ConfigLayers {

  def configLogLayer(loggerName: String): ULayer[Logging] = Slf4jLogger.make(
    (_, logEntry) => logEntry,
    Some(loggerName)
  )

  lazy val appConfigLayer: TaskLayer[AppConfig] = configLogLayer("appConfig") >>> appConfig.live

}
