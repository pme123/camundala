package pme123.camundala.config

import java.io.File

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, ConfigDescriptor, config}
import zio.console.Console

object appConfig {
  type AppConfig = Has[Service]

  trait Service {
    def get(): Task[AppConf]
  }

  def get(): RIO[AppConfig, AppConf] =
    ZIO.accessM(_.get.get())

  case class ConfWrapper(camundala: AppConf)

  case class AppConf(servicesConf: ServicesConf)

  case class ServicesConf(host: String, port: Int = 8888) {
    val url = s"$host:$port"
  }

  /**
    * Reads the App Authentication from the `twitter-auth.conf`.
    */
  lazy val live: RLayer[Console, AppConfig] = {

    val serviceConf =
      (string("host") |@| int("port")
        ) (ServicesConf.apply, ServicesConf.unapply)

    val appConf =
      (nested("services")(serviceConf)
        ) (AppConf.apply, AppConf.unapply)

    val confWrapper =
      (nested("camundala")(appConf)
        ) (ConfWrapper.apply, ConfWrapper.unapply)

    lazy val sourceLayer: TaskLayer[Config[ConfWrapper]] = TypesafeConfig.fromDefaultLoader(confWrapper)

    ZLayer.fromService(console =>
      new Service {
        def get(): Task[AppConf] =
          config[ConfWrapper]
            .map(_.camundala)
            .provideLayer(sourceLayer)
      })
  }

}