package pme123.camundala.services

import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.{AppConfig, ServicesConf}
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.interop.catz._
import zio.interop.catz.implicits._

object httpServer {
  type HttpServer = Has[Service]

  trait Service {
    def serve(): Task[Unit]
  }

  def serve(): RIO[HttpServer, Unit] =
    ZIO.accessM(_.get.serve())

  type HttpServerDeps = AppConfig with Console with Clock

  /**
    * Live Implementation that accesses Twitter and tweets for real to http://twitter.com/#!/camunda_demo
    */
  lazy val live: RLayer[HttpServerDeps, HttpServer] =
    ZLayer.fromServices[appConfig.Service, Console.Service, Clock.Service, Service] {
      (config, console, clock) =>

        val dsl: Http4sDsl[Task] = Http4sDsl[Task]

        import dsl._

        lazy val routes: HttpRoutes[Task] =
          HttpRoutes.of[Task] {
            case GET -> Root / "hello" =>
              Ok("Hello")
          }

        def server(servicesConf: ServicesConf) =
          ZIO.runtime[Any]
            .flatMap {
              implicit rts =>
                BlazeServerBuilder[Task]
                  .bindHttp(servicesConf.port, servicesConf.host)
                  .withHttpApp(routes.orNotFound)
                  .serve
                  .compile
                  .drain
            }

        new Service {
          def serve(): Task[Unit] =
            for {
              config <- config.get()
              tweet <- server(config.servicesConf)
              _ <- console.putStrLn(s"$tweet sent")
            } yield ()
        }


    }

}