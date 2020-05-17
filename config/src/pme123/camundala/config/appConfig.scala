package pme123.camundala.config

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, config}
import zio.logging.Logging

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
  lazy val live: RLayer[Logging, AppConfig] = {

    val serviceConf =
      (string("host") |@| int("port")
        ) (ServicesConf.apply, ServicesConf.unapply)

    val appConf =
      nested("services")(serviceConf)(AppConf.apply, AppConf.unapply)

    val confWrapper =
      nested("camundala")(appConf)(ConfWrapper.apply, ConfWrapper.unapply)

    lazy val sourceLayer: TaskLayer[Config[ConfWrapper]] = TypesafeConfig.fromDefaultLoader(confWrapper)
    import zio.config.generateDocsWithValue

    ZLayer.fromService(log =>
      () => config[ConfWrapper]
        .map(_.camundala)
        .tap(config => log.info(s"Camundala Configuration:\n$config"))
        .provideLayer(sourceLayer))
  }

}