package pme123.camundala.camunda

import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import zio.{ZIO, ZManaged}
import zio.console.Console

trait ZSpringApp extends zio.App {


  /**
    * create SpringApplication as a ZManaged Resource.
    */
  protected def managedSpringApp(clazz: Class[_], args: List[String] = List.empty)(implicit c: Console.Service): ZManaged[Any, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      c.putStrLn("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      c.putStrLn("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll(ex =>
          c.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
        )
    )
}
