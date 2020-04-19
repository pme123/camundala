package pme123.camundala.twitter

import com.danielasfregola.twitter4s.TwitterRestClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import twitterConfig.TwitterConfig
import zio._
import zio.console.Console

object twitterApi {
  type TwitterApi = Has[Service]

  trait Service {
    def createTweet(tweet: String): Task[Unit]
  }

  def createTweet(tweet: String): RIO[TwitterApi, Unit] =
    ZIO.accessM(_.get.createTweet(tweet))

  type TwitterApiDeps = TwitterConfig with Console

  /**
    * Live Implementation that accesses Twitter and tweets for real to http://twitter.com/#!/camunda_demo
    */
  lazy val live: RLayer[TwitterApiDeps, TwitterApi] =
    ZLayer.fromServices[Console.Service, twitterConfig.Service, Service] {
      (console, twitterConfig) =>
        new Service {
          def createTweet(tweet: String): Task[Unit] =
            for {
              config <- twitterConfig.auth()
              tweet <- ZIO.fromFuture { _ =>
                val consumerToken = ConsumerToken(config.consumerToken.key, config.consumerToken.value)
                val accessToken = AccessToken(config.accessToken.key, config.accessToken.value)
                val twitter = TwitterRestClient(consumerToken, accessToken)
                twitter.createTweet(tweet)
              }
              _ <- console.putStrLn(s"$tweet sent")
            } yield ()
        }
    }

  /**
    * Offline Implementation that just writes the tweet to the console.
    */
  lazy val offline: RLayer[Console, TwitterApi] =
    ZLayer.fromService[Console.Service, Service] {
      console =>
        new Service {
          def createTweet(tweet: String): Task[Unit] =
            for {
              tweet <- ZIO.succeed {
                s"""|
                    |
                    |${"#" * 20}
                    |
                    |NOW WE WOULD TWEET:
                    |'$tweet'
                    |
                    |
                    |${"#" * 20}
                    |
                    |
                    |""".stripMargin
              }
              _ <- console.putStrLn(tweet)
            } yield ()
        }
    }
}