package pme123.camundala.twitter

import java.io.File

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, ConfigDescriptor, config}

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
  lazy val live: TaskLayer[TwitterConfig] = {

    val tokenConfig =
      (string("key") |@|
        string("value")
        ) (Token.apply, Token.unapply)

    val twitterAuthConfig: ConfigDescriptor[String, String, TwitterAuth] =
      (nested("consumerToken")(tokenConfig) |@|
        nested("accessToken")(tokenConfig)
        ) (TwitterAuth.apply, TwitterAuth.unapply)

    lazy val sourceLayer: TaskLayer[Config[TwitterAuth]] = TypesafeConfig.fromHoconFile(new File("twitter-auth.conf"), twitterAuthConfig)

    ZLayer.succeed(
      new Service {
        def auth(): Task[TwitterAuth] =
          config[TwitterAuth]
            .provideLayer(sourceLayer)
      })
  }

}