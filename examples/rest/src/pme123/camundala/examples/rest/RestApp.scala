package pme123.camundala.examples.rest

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.CamundaApp
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.StandardApp
import zio.{ExitCode, ZIO}

import scala.annotation.nowarn

@SpringBootApplication
@EnableProcessApplication
class RestApp

object RestApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    CamundaApp
      .managedSpringApp(classOf[RestApp], args)
      .useForever
      .provideLayer(
        ModelLayers.logLayer("RestApp", "pme123.camundala.examples.rest")
      )
      .exitCode

}
