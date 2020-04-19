package pme123.camundala.examples.twitter

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.ZSpringApp
import pme123.camundala.config.appConfig
import pme123.camundala.services.httpServer
import zio.ZIO
import zio.clock.Clock
import zio.console.Console

@SpringBootApplication
@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- http.fork
      _ <- spring(args).useForever
    } yield ())
      .provideLayer((appConfig.live ++ Clock.live ++ Console.live) >>> (httpServer.live ++ Console.live))
      .fold(
        _ => 1,
        _ => 0
      )

  private lazy val http = httpServer.serve()
    .provideLayer((appConfig.live ++ Clock.live ++ Console.live) >>> httpServer.live)

  private def spring(args: List[String]) = managedSpringApp(classOf[TwitterApp], args)
    .provideLayer(Console.live)
}
