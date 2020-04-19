package pme123.camundala.examples.twitter

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.ZSpringApp
import zio.ZIO
import zio.console.Console

@SpringBootApplication
@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    managedSpringApp(classOf[TwitterApp], args).useForever
      .provideLayer(Console.live)
      .fold(
        _ => 1,
        _ => 0
      )

}
