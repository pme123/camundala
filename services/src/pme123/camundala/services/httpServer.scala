package pme123.camundala.services

import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl._
import org.http4s.implicits._
import org.http4s.multipart.Multipart
import pme123.camundala.camunda.httpDeployClient.HttpDeployClient
import pme123.camundala.camunda.{DeployFile, DeployRequest, JsonEnDecoders, httpDeployClient}
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.{AppConfig, ServicesConf}
import pme123.camundala.model.bpmn._
import pme123.camundala.model.register.deployRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import zio.logging.Logging


object httpServer
  extends JsonEnDecoders {
  type HttpServer = Has[Service]

  trait Service {
    def serve(): Task[Unit]
  }

  def serve(): RIO[HttpServer, Unit] =
    ZIO.accessM(_.get.serve())

  type HttpServerDeps = AppConfig with DeployRegister with HttpDeployClient with Logging

  /**
    * http4s Implementation
    */
  lazy val live: RLayer[HttpServerDeps, HttpServer] =
    ZLayer.fromServices[appConfig.Service, deployRegister.Service, httpDeployClient.Service, logging.Logger[String], Service] {
      (configServ, deplRegister, deployService, log) =>

        val dsl: Http4sDsl[Task] = Http4sDsl[Task]
        import dsl._


        lazy val routes: HttpRoutes[Task] = HttpEndpointServer.allRoutes <+>
          (HttpRoutes.of[Task] {
            case req@POST -> Root / "deployment" / "create" =>
              req.decode[Multipart[Task]] { m =>
                deployMultipart(m)
                  .foldM(_ =>
                    InternalServerError(),
                    Ok(_))
              }
            case req@POST -> Root / deployIdStr / "deployment" / "create" =>
              req.decode[Multipart[Task]] { m =>
                deployIdFromStr(deployIdStr).flatMap(deployId =>
                  deployMultipart(m, deployId)
                    .foldM(_ =>
                      InternalServerError(),
                      Ok(_))
                )
              }
          })

        def deployMultipart(m: Multipart[Task], deployId: DeployId = DeployId) = {
          import pme123.camundala.camunda.DeployRequest._

          def forName(m: Multipart[Task], name: PropKey) = {
            m.parts.collectFirst { case p if p.name.contains(name.value) =>
              p.body.compile.toVector
                .map(v => Some(new String(v.toArray)))
                .catchAll(e =>
                  log.info(s"Problem receiving ${e.getMessage}") *>
                    ZIO.succeed(None)
                )
            }.getOrElse(ZIO.succeed(None))
          }

          (for {
            files <- ZIO.foreach(m.parts.filter(p => p.name.isEmpty || !ReservedKeywords.contains(p.name.get)))(p =>
              p.filename.map(fn => p.body.compile.toVector.flatMap(v => filePathFromStr(fn).map(x => DeployFile(x, v))))
                .getOrElse(Task.fail(InvalidRequestException(s"No file name found in the deployment resource described by form parameter '${p.name.getOrElse("")}'."))))
            file <- ZIO.fromOption(files.headOption).mapError(_ => InvalidRequestException(s"No file in the Multipart of the Request from the Modeler."))
            maybeBpmnId <- forName(m, DeploymentName)
            bpmnIdStr <- ZIO.fromOption(maybeBpmnId).mapError(_ => HttpServerException(s"BpmnId ($DeploymentName) must be set!"))
            bpmnId <- bpmnIdFromStr(bpmnIdStr)
            enableDuplFiltering <- forName(m, EnableDuplicateFiltering).map(_.exists(_.toBoolean))
            deployChangedOnly <- forName(m, DeployChangedOnly).map(_.exists(_.toBoolean))
            deploySource <- forName(m, DeploymentSource)
            tenantId <- forName(m, tenantId)
            config <- configServ.get()
            deploy <- deplRegister.requestDeploy(deployId)
            camundaEndpoint = deploy.map(_.camundaEndpoint).getOrElse(config.camundaConf.rest)
            deployResult <- deployService.deploy(DeployRequest(bpmnId, file, camundaEndpoint, enableDuplFiltering, deployChangedOnly, deploySource, tenantId))
          } yield deployResult.asJson)
            .tapError(e => log.error(s"Error: $e", Cause.fail(e)))
        }

        def server(servicesConf: ServicesConf) =
          ZIO.runtime[Any]
            .flatMap {
              implicit rts =>

                import org.http4s.implicits._
                import org.http4s.server.blaze._

                import scala.concurrent.ExecutionContext.global

                BlazeServerBuilder.apply[Task](global)
                  .bindHttp(servicesConf.port, servicesConf.host)
                  .withHttpApp(routes.orNotFound)
                  .serve
                  .compile
                  .drain

            }

        () =>
          for {
            config <- configServ.get()
            _ <- server(config.servicesConf)
            _ <- log.info(s"HTTP server started on port: ${config.servicesConf.url}")
          } yield ()
    }

  case class HttpServerException(msg: String) extends CamundalaException

}
