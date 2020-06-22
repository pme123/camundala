package pme123.camundala.services

import java.io.File

import cats.implicits._
import io.circe.generic.auto._
import org.http4s.HttpRoutes
import pme123.camundala.camunda.DeployResult
import pme123.camundala.camunda.httpDeployClient.HttpDeployClient
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn._
import pme123.camundala.model.register.deployRegister.DeployRegister
import sttp.model.Part
import sttp.tapir.Endpoint
import sttp.tapir.docs.openapi._
import sttp.tapir.generic.Configuration
import sttp.tapir.json.circe._
import sttp.tapir.openapi.circe.yaml._
import sttp.tapir.redoc.http4s.RedocHttp4s
import sttp.tapir.server.http4s.ztapir._
import sttp.tapir.swagger.http4s.SwaggerHttp4s
import sttp.tapir.ztapir._
import zio._
import zio.interop.catz._
import zio.logging.Logging

object HttpEndpoint {


  lazy val redocs: ZEndpoint[Unit, Unit, String] =
    endpoint.get
      .in("")
      .out(htmlBodyUtf8)
      .name("Open API Documentation")
      .description("Generated Open API Documentation (Redoc).")

  lazy val docs: ZEndpoint[Unit, Unit, String] =
    endpoint.get
      .in("docs")
      .out(htmlBodyUtf8)
      .name("Swagger API Documentation")
      .description("Generated Open API Documentation (Swagger). You can also try your API with this.")

  lazy val camundaDeployDummy: ZEndpoint[Unit, Unit, String] =
    endpoint.get
      .in("deployment")
      .out(stringBody)
      .name("Check Server from Modeler")
      .description("Checks if the Server is there from the Camunda Modeler.")


  lazy val camundaDeployDummyWithId: ZEndpoint[String, Unit, String] =
    endpoint.get
      .in(path[String].name("deployId").example("remote") / "deployment")
      .out(stringBody)
      .name("Check Server from Modeler with DeployId")
      .description("Checks if the Server is there from the Camunda Modeler, when defining a DeployId.")


  /*
  Multipart does not WORK:
  - file field has no static name
  - case class did not work yet - no exception
  - HOWEVER we could document it
  */
  implicit def conf: Configuration = Configuration.default.withSnakeCaseMemberNames

  case class DeployMultipart(`deployment-name`: String,
                             `enable-duplicate-filterung`: Boolean = false,
                             `deploy-changed-only`: Boolean = false,
                             `deployment-source`: Option[String] = None,
                             `tenant-id`: Option[String] = None,
                             bpmn: Part[File]
                            )

  lazy val deployCreate: Endpoint[DeployMultipart, Unit, DeployResult, Nothing] =
    endpoint.post
      .in("deployment" / "create")
      .in(multipartBody[DeployMultipart]) // only in Intellij a problem
      .out(jsonBody[DeployResult])
      .name("Create Deployment from Modeler")
      .description("This creates a Deployment from the Modeler Deploy Dialog.\nThe name of the file part is not static.")

  lazy val openApi: String = List(redocs, docs, camundaDeployDummy, camundaDeployDummyWithId, deployCreate).toOpenAPI("Camundala API", "1.0")
    .toYaml

}

object HttpEndpointServer {

  import HttpEndpoint._

  lazy val allRoutes: HttpRoutes[Task] =
    camundaDeployDummyLogic <+> camundaDeployDummyWithIdLogic <+>
      openApiSwaggerRoutes <+> openApiRedocRoutes

  // lazy val allDeployRoutes: URIO[DeployDeps, HttpRoutes[Task]] = deployCreateLogic
  private lazy val openApiSwaggerRoutes: HttpRoutes[Task] = new SwaggerHttp4s(openApi).routes
  private lazy val openApiRedocRoutes: HttpRoutes[Task] = new RedocHttp4s("Camundala API", openApi, "redocs.yaml").routes

  private lazy val camundaDeployDummyLogic: HttpRoutes[Task] =
    camundaDeployDummy.toRoutes(_ => UIO("Services are up and running"))

  private lazy val camundaDeployDummyWithIdLogic: HttpRoutes[Task] =
    camundaDeployDummyWithId.toRoutes(deployId =>
      UIO(s"Services are up and running for DeployId: $deployId"))

  type DeployDeps = HttpDeployClient with DeployRegister with AppConfig with Logging
  /*
    lazy val deployCreateLogic: URIO[DeployDeps, HttpRoutes[Task]] =
      deployCreate.toRoutesR[HttpDeployClient with DeployRegister with AppConfig with Logging](dm =>
        deployMultipart(dm)
      )


    def deployMultipart(parts: Seq[AnyPart], deployId: DeployId = DeployId): ZIO[DeployDeps, Unit, DeployResult] = {
      println(s"DM: $parts")

      def forName(name: PropKey): ZIO[Logging, Nothing, Option[String]] = {
        parts.collectFirst { case p if p.name == name.value =>
          Task(Some(p.body.toString))
            .catchAll(e =>
              log.warn(s"Problem extracting body for $name: ${e.getMessage}") *>
                ZIO.succeed(None)
            )
        }.getOrElse(ZIO.succeed(None))
      }
      import pme123.camundala.camunda.DeployRequest._
      (for {
        files <- ZIO.foreach(parts.filter(p => p.fileName.nonEmpty && !ReservedKeywords.contains(p.name))) {
          part: AnyPart =>
            part.body match {
              case ba: Array[Byte] =>
                filePathFromStr(part.fileName.get).map(DeployFile(_, ba.toVector))
            }
        }
        file <- ZIO.fromOption(files.headOption).mapError(_ => InvalidRequestException(s"No file in the Multipart of the Request from the Modeler."))
        maybeBpmnId <- forName(DeploymentName)
        bpmnIdStr <- ZIO.fromOption(maybeBpmnId).mapError(_ => HttpServerEndpointException(s"BpmnId ($DeploymentName) must be set!"))
        bpmnId <- bpmnIdFromStr(bpmnIdStr)
        enableDuplFiltering <- forName(EnableDuplicateFiltering).map(_.exists(_.toBoolean))
        deployChangedOnly <- forName(DeployChangedOnly).map(_.exists(_.toBoolean))
        deploySource <- forName(DeploymentSource)
        tenantId <- forName(tenantId)
        config <- appConfig.get()
        deploy <- deployRegister.requestDeploy(deployId)
        camundaEndpoint = deploy.map(_.camundaEndpoint).getOrElse(config.camundaConf.rest) //TODO Get rid of this config
        deployResult <- httpDeployClient.deploy(DeployRequest(bpmnId, file, camundaEndpoint, enableDuplFiltering, deployChangedOnly, deploySource, tenantId))
      } yield deployResult)
        .catchAll(e => log.error(s"Error: $e", Cause.fail(e)) *> ZIO.fail(()))
    }
  */
}

case class HttpServerEndpointException(msg: String) extends CamundalaException
