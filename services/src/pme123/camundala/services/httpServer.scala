package pme123.camundala.services

import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.multipart.Multipart
import org.http4s.server.blaze.BlazeServerBuilder
import pme123.camundala.camunda.{DeployFile, DeployRequest, deploymentService}
import pme123.camundala.camunda.deploymentService._
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.{AppConfig, ServicesConf}
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.logging.Logging

object httpServer {
  type HttpServer = Has[Service]

  trait Service {
    def serve(): Task[Unit]
  }

  def serve(): RIO[HttpServer, Unit] =
    ZIO.accessM(_.get.serve())

  type HttpServerDeps = AppConfig with DeploymentService with Logging

  /**
    * http4s Implementation
    */
  lazy val live: RLayer[HttpServerDeps, HttpServer] =
    ZLayer.fromServices[appConfig.Service, deploymentService.Service, logging.Logger[String], Service] {
      (config, deployService, log) =>

        val dsl: Http4sDsl[Task] = Http4sDsl[Task]
        import dsl._

        lazy val routes: HttpRoutes[Task] =
          HttpRoutes.of[Task] {
            case GET -> Root =>
              Ok("Services are up and running")
            case req@POST -> Root / "deployment" / "create" =>
              req.decode[Multipart[Task]] { m =>
                deployMultipart(m)
                  .foldM(e =>
                    InternalServerError(),
                    Ok(_))
              }
          }

        def deployMultipart(m: Multipart[Task]) = {
          import pme123.camundala.camunda.DeployRequest._

          def forName(m: Multipart[Task], name: String) = {
            m.parts.collectFirst { case p if p.name.contains(name) =>
              p.body.compile.toVector
                .map(v => Some(new String(v.toArray)))
                .catchAll(e =>
                  log.info(s"Problem receiving ${e.getMessage}") *>
                    ZIO.none
                )
            }.getOrElse(ZIO.none)
          }

          (for {
            files <- ZIO.foreach(m.parts.filter(p => p.name.isEmpty || !RESERVED_KEYWORDS.contains(p.name.get)))(p =>
              p.filename.map(fn => p.body.compile.toVector.map(v => DeployFile(fn, v)))
                .getOrElse(Task.fail(InvalidRequestException(s"No file name found in the deployment resource described by form parameter '${p.name.getOrElse("")}'."))))
            deployName <- forName(m, DEPLOYMENT_NAME)
            enableDuplFiltering <- forName(m, ENABLE_DUPLICATE_FILTERING).map(_.exists(_.toBoolean))
            deployChangedOnly <- forName(m, DEPLOY_CHANGED_ONLY).map(_.exists(_.toBoolean))
            deploySource <- forName(m, DEPLOYMENT_SOURCE)
            tenantId <- forName(m, TENANT_ID)
            deployResult <- deployService.deploy(DeployRequest(deployName, enableDuplFiltering, deployChangedOnly, deploySource, tenantId, files.toSet))
          } yield deployResult.asJson)
            .tap(j => log.info(s"JSON: $j"))
            .tapError(e => log.error(s"Error: $e") *> ZIO.effect(e.printStackTrace()))
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
              _ <- server(config.servicesConf)
              _ <- log.info(s"HTTP server started on port: ${config.servicesConf.url}")
            } yield ()
        }
    }
}