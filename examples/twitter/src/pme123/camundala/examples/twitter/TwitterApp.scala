package pme123.camundala.examples.twitter

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.ConfigurableApplicationContext
import zio.console.Console
import zio.{ZIO, ZManaged, console}

@SpringBootApplication
@EnableProcessApplication
class TwitterApp

object TwitterApp extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    managedSpringApp(args).useForever
      .provideLayer(Console.live)
      .fold(
        _ => 1,
        _ => 0
      )

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  private def managedSpringApp(args: List[String]): ZManaged[Console, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      console.putStrLn("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(classOf[TwitterApp], args: _*)
        )
    )(ctx =>
      console.putStrLn("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll(ex =>
          console.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
        )
    )

}
