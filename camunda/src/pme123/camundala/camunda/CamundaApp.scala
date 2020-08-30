package pme123.camundala.camunda

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import zio.logging.{Logging, log}
import zio.{Cause, ExitCode, ZIO, ZManaged}

@SpringBootApplication(scanBasePackages = Array("pme123.camundala.camunda"))
class CamundaApp {}

object CamundaApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    managedSpringApp(classOf[CamundaApp], args)
      .useForever
      .provideLayer(CamundaLayers.logLayer("CamundaApp"))
      .exitCode

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  def managedSpringApp(clazz: Class[_], args: List[String] = List.empty): ZManaged[Logging, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      log.info("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      log.info("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll((ex: Throwable) =>
          log.error(s"Problem shutting down the Spring Container.\n${ex.getMessage}", Cause.fail(ex))
        )
    )

}



