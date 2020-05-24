package pme123.camundala.config

import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.ModelLayers
import zio.logging.Logging
import zio.{TaskLayer, ULayer}

object ConfigLayers {

  def configLogLayer(loggerName: String): ULayer[Logging] =
    ModelLayers.logLayer(loggerName, "pme123.camundala.config")

  lazy val appConfigLayer: TaskLayer[AppConfig] = configLogLayer("appConfig") >>> appConfig.live

}
