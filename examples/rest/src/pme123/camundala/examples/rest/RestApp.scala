package pme123.camundala.examples.rest

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.services.{StandardApp, ZSpringApp}
import zio.ZIO

@SpringBootApplication
@EnableProcessApplication
class RestApp

object RestApp extends ZSpringApp {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    StandardApp.managedSpringApp(classOf[RestApp], args).useForever
      .fold(
        _ => 1,
        _ => 0
      )
}
