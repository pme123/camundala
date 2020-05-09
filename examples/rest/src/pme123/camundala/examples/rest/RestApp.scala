package pme123.camundala.examples.rest

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.ZSpringApp
import zio.ZIO
import zio.console.Console

@SpringBootApplication
@EnableProcessApplication
class RestApp

object RestApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    managedSpringApp(classOf[RestApp], args)(Console.Service.live).useForever
      .fold(
        _ => 1,
        _ => 0
      )
}
