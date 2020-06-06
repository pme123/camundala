package pme123.camundala.services

import org.springframework.boot.autoconfigure.SpringBootApplication
import zio.{ExitCode, ZIO}

object CamundaApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    (for {
      _ <- StandardApp.managedSpringApp(classOf[CamundaApp], args).useForever
    } yield ()).provideLayer(ServicesLayers.logLayer("CamundalaApp"))
      .fold(
        _ => ExitCode.failure,
        _ => ExitCode.success
      )
}

@SpringBootApplication
class CamundaApp {}
