package pme123.camundala.services

import org.springframework.boot.autoconfigure.SpringBootApplication
import zio.ZIO

object CamundaApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- StandardApp.managedSpringApp(classOf[CamundaApp], args).useForever
    } yield ()).provideLayer(ServicesLayers.logLayer("CamundalaApp"))
      .fold(
        _ => 1,
        _ => 0
      )

}

@SpringBootApplication
class CamundaApp {}
