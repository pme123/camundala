package pme123.camundala.config

import pme123.camundala.config.appConfig.AppConfig
import zio.{TaskLayer, ULayer}
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging

object ConfigLayers {

  def configLogLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )

  lazy val appConfigLayer: TaskLayer[AppConfig] = configLogLayer("appConfig") >>> appConfig.live

}
