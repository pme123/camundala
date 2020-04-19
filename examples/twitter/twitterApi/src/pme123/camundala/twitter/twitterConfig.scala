package pme123.camundala.twitter

import java.io.File

import zio._
import zio.config.ConfigDescriptor._
import zio.config.typesafe.TypesafeConfig
import zio.config.{Config, ConfigDescriptor, config}
import zio.console.Console

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
  def live(authFile: File): RLayer[Console, TwitterConfig] = {

    val tokenConfig =
      (string("key") |@|
        string("value")
        ) (Token.apply, Token.unapply)

    val twitterAuthConfig: ConfigDescriptor[String, String, TwitterAuth] =
      (nested("consumerToken")(tokenConfig) |@|
        nested("accessToken")(tokenConfig)
        ) (TwitterAuth.apply, TwitterAuth.unapply)

    lazy val sourceLayer: TaskLayer[Config[TwitterAuth]] = TypesafeConfig.fromHoconFile(authFile, twitterAuthConfig)

    ZLayer.fromService(console =>
      new Service {
        def auth(): Task[TwitterAuth] =
          console.putStrLn(    s"AuthConfig from: ${authFile.getAbsolutePath}") *>
          config[TwitterAuth]
            .provideLayer(sourceLayer)
      })
  }

}