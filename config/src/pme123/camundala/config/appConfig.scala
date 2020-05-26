package pme123.camundala.config

import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.CamundaEndpoint
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

  case class AppConf(basePath: String, servicesConf: ServicesConf, camundaConf: CamundaConf)

  case class ServicesConf(host: String, port: Int = 8888) {
    val url = s"$host:$port"
  }

  case class CamundaConf(rest: RestHost)

  case class RestHost(url: String, user: String, password: String) {

    def toCamundaEndpoint: ZIO[Any, ModelException, CamundaEndpoint] =
      for{
        curl <-urlFromStr(url)
        cuser <-  usernameFromStr(user)
        pwd <- passwordFromStr(password)
      } yield
        CamundaEndpoint(curl, cuser, Sensitive(pwd))
  }

  /**
    * Reads the App Authentication from the `twitter-auth.conf`.
    */
  lazy val live: RLayer[Logging, AppConfig] = {


    val camundaRestConf =
      (string("url") |@|
        string("user") |@|
        string("password")
        ) (RestHost.apply, RestHost.unapply)

    val camundaConf =
      nested("rest")(camundaRestConf)(CamundaConf.apply, CamundaConf.unapply)

    val serviceConf =
      (string("host") |@| int("port")
        ) (ServicesConf.apply, ServicesConf.unapply)


    val appConf =
      (string("basePath") |@|
        nested("services")(serviceConf) |@|
        nested("camunda")(camundaConf)) (AppConf.apply, AppConf.unapply)

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