package pme123.camundala.twitter

import java.io.File

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, ConfigDescriptor, config}
import zio.logging.Logging

object twitterConfig {
  type TwitterConfig = Has[Service]

  trait Service {
    def auth(): Task[TwitterAuth]
  }

  def auth(): RIO[TwitterConfig, TwitterAuth] =
    ZIO.accessM(_.get.auth())

  case class TwitterAuth(consumerToken: Token, accessToken: Token)

  case class Token(key: String, value: String)

  /**
    * Reads the Twitter Authentication from the `twitter-auth.conf`.
    */
  def live(authFile: File): RLayer[Logging, TwitterConfig] = {

    val tokenConfig =
      (string("key") |@|
        string("value")
        ) (Token.apply, Token.unapply)

    val twitterAuthConfig: ConfigDescriptor[TwitterAuth] =
      (nested("consumerToken")(tokenConfig) |@|
        nested("accessToken")(tokenConfig)
        ) (TwitterAuth.apply, TwitterAuth.unapply)

    lazy val sourceLayer: TaskLayer[Config[TwitterAuth]] = TypesafeConfig.fromHoconFile(authFile, twitterAuthConfig)

    ZLayer.fromService(log =>
      () => log.info(s"AuthConfig from: ${authFile.getAbsolutePath}") *>
        config[TwitterAuth]
          .provideLayer(sourceLayer))
  }

}
