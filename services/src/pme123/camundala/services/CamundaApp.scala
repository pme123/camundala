package pme123.camundala.services

import org.springframework.boot.autoconfigure.SpringBootApplication
import zio.{ExitCode, ZIO}

import scala.annotation.nowarn

object CamundaApp extends zio.App {

  @nowarn("cat=w-flag-dead-code")
  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    StandardApp.managedSpringApp(classOf[CamundaApp], args).useForever
      .provideLayer(ServicesLayers.logLayer("CamundalaApp"))
      .fold(
        _ => ExitCode.failure,
        _ => ExitCode.success
      )
}

@SpringBootApplication
class CamundaApp {}
