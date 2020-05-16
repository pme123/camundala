package pme123.camundala.camunda

import java.util.concurrent.TimeUnit

import io.circe
import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}
import io.circe.generic.semiauto._
import pme123.camundala.app.sttpBackend.SttpTaskBackend
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.xml.{ValidateWarning, ValidateWarnings}
import pme123.camundala.model.bpmn.{BpmnId, CamundalaException}
import pme123.camundala.model.deploy.Deploy
import sttp.client.circe._
import sttp.client.{multipart, _}
import zio.clock.Clock
import zio.duration._
import zio.logging.Logging
import zio.{logging, _}

object httpDeployClient {
  type HttpDeployClient = Has[Service]

  trait Service {
    def deploy(deploy: Deploy): Task[Seq[DeployResult]]

    def undeploy(bpmnId: BpmnId): Task[Unit]

    def deployments(): Task[Seq[DeployResult]]
  }

  def deploy(deploy: Deploy): RIO[HttpDeployClient, Seq[DeployResult]] =
    ZIO.accessM(_.get.deploy(deploy))

  def undeploy(bpmnId: BpmnId): RIO[HttpDeployClient, Unit] =
    ZIO.accessM(_.get.undeploy(bpmnId))

  def deployments(): RIO[HttpDeployClient, Seq[DeployResult]] =
    ZIO.accessM(_.get.deployments())

  type HttpDeployClientDeps = Has[SttpTaskBackend] with BpmnService with Logging with Clock

  /**
    * http4s Implementation
    */
  lazy val live: RLayer[HttpDeployClientDeps, HttpDeployClient] =
    ZLayer.fromServices[SttpTaskBackend, bpmnService.Service, logging.Logger[String], Clock.Service, Service] {
      (backend, bpmnServ, log, clock) =>
        implicit def sttpBackend: SttpBackend[Task, Nothing, NothingT] = backend

        import DeployRequest._

        new Service {
          def deploy(deploy: Deploy): Task[Seq[DeployResult]] =
            ZIO.foreach(deploy.bpmns) { bpmn =>
                for {
                  endpoint <- ZIO.fromOption(deploy.maybeRemote).mapError(_ => HttpDeployClientException(s"There is no Remote Configuration for ${deploy.id}"))
                  uri = uri"${endpoint.url.value}/deployment/create"
                  logMsg = s"Deploy ${bpmn.id} to ${endpoint.url.value}"
                  mergeResult <- bpmnServ.mergeBpmn(bpmn.id)
                  _ <- log.info(logMsg)
                  staticFiles = bpmn.staticFiles.map(st => multipart(st.fileName.value, StreamHelper.inputStream(st)).fileName(st.fileName.value))
                  body = Seq(
                    multipart(DeploymentName, bpmn.id.value),
                    multipart(EnableDuplicateFiltering, "false"),
                    multipart(DeployChangedOnly, "true"),
                    multipart(DeploymentSource, "Camundala Client"),
                    multipart(bpmn.xml.fileName.value, StreamHelper.inputStream(mergeResult.xmlElem)).fileName(bpmn.xml.fileName.value),
                  ) ++ staticFiles
                  deployResult <- basicRequest
                    .auth.basic(endpoint.user, endpoint.password.value)
                    .multipartBody(body)
                    .response(asJson[DeployResult])
                    .post(uri)
                    .send()
                    .tapError(error =>
                      for {
                        t <- clock.currentTime(TimeUnit.SECONDS)
                        _ <- log.info(s"Failing attempt (${t % 100} s): ${error.getMessage}")
                      } yield ()
                    )
                    .tap { r =>
                      log.debug(s"Response with Status ${r.code}\n${r.body}")
                    }
                    .retry(Schedule.recurs(5) && Schedule.exponential(1.second))
                    .map(_.body)
                    .flatMap {
                      case Left(error: ResponseError[circe.Error]) =>
                        ZIO.fail(HttpDeployClientException(s"Could not Parse DeployResult\n${error.getMessage}\n${error.body}", Some(error)))
                      case Right(value) =>
                        ZIO.succeed(value)
                    }
                } yield
                  deployResult.withWarnings(mergeResult.warnings)
            }.catchAll(er => UIO(er.printStackTrace()) *> ZIO.fail(er match {
              case ex: HttpDeployClientException => ex
              case _ => HttpDeployClientException(s"There is Problem with ${deploy.id} - see the stack trace")
            })).provideLayer(ZLayer.succeed(clock))

          override def undeploy(bpmnId: BpmnId): Task[Unit] = ???

          override def deployments(): Task[Seq[DeployResult]] = ???
        }
    }

  case class HttpDeployClientException(msg: String,
                                       override val cause: Option[Throwable] = None)
    extends CamundalaException

  implicit val deployResultDecoder: Decoder[DeployResult] = (c: HCursor) => for {
    id <- c.downField("id").as[String]
    name <- c.downField("name").as[String]
    deploymentTime <- c.downField("deploymentTime").as[String]
    source <- c.downField("source").as[Option[String]]
    tenantId <- c.downField("tenantId").as[Option[String]]
  } yield {
    DeployResult(id, name, deploymentTime, source, tenantId)
  }

  implicit val validationWarningsDecoder: Decoder[ValidateWarnings] = deriveDecoder[ValidateWarnings]
  implicit val validationWarningDecoder: Decoder[ValidateWarning] = deriveDecoder[ValidateWarning]

}
