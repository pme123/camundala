package pme123.camundala.config

import pme123.camundala.model.deploy.{CamundaEndpoint, Sensitive}
import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, config, _}
import zio.logging.Logging

object appConfig {
  type AppConfig = Has[Service]

  trait Service {
    def get(): Task[AppConf]
  }

  def get(): RIO[AppConfig, AppConf] =
    ZIO.accessM(_.get.get())

  case class ConfWrapper(camundala: AppConf)

  case class AppConf(servicesConf: ServicesConf/*, camundaConf: CamundaConf*/)

  case class ServicesConf(host: String, port: Int = 8888) {
    val url = s"$host:$port"
  }

  case class CamundaConf(rest: CamundaEndpoint)


  /**
    * Reads the App Authentication from the `twitter-auth.conf`.
    */
  lazy val live: RLayer[Logging, AppConfig] = {
/*
    val sensitiveConf = nonEmpty(string("secret"))(Sensitive.apply, Sensitive.unapply)

    val camundaRestConf =
      (url(string("url")) |@|
        nonEmpty(string("user")) |@|
        nested("password")(sensitiveConf)
        ) (CamundaEndpoint.apply, CamundaEndpoint.unapply)
    val camundaConf =
      nested("rest")(camundaRestConf)(CamundaConf.apply, CamundaConf.unapply)
*/
    val serviceConf =
      (string("host") |@| int("port")
        ) (ServicesConf.apply, ServicesConf.unapply)


    val appConf =
      (nested("services")(serviceConf) /*|@| nested("camunda")(camundaConf)*/) (AppConf.apply, AppConf.unapply)

    val confWrapper =
      nested("camundala")(appConf)(ConfWrapper.apply, ConfWrapper.unapply)

    lazy val sourceLayer: TaskLayer[Config[ConfWrapper]] = TypesafeConfig.fromDefaultLoader(confWrapper)

    ZLayer.fromService(log =>
      () => config[ConfWrapper]
        .map(_.camundala)
        .tap(config => log.info(s"Camundala Configuration:\n$config"))
        .provideLayer(sourceLayer))
  }

}