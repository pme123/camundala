package pme123.camundala.examples.rest

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.model.ModelLayers
import pme123.camundala.services.StandardApp
import zio.{ExitCode, ZIO}

@SpringBootApplication
@EnableProcessApplication
class RestApp

object RestApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    StandardApp.managedSpringApp(classOf[RestApp], args).useForever
      .fold(
        _ => ExitCode.failure,
        _ => ExitCode.success
      ).provideLayer(ModelLayers.logLayer("RestApp", "pme123.camundala.examples.rest"))
}
