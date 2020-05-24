package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import eu.timepit.refined.auto._
import io.circe.generic.semiauto._
import io.circe.{Decoder, HCursor}
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.service.restService
import pme123.camundala.camunda.service.restService.QueryParams.Params
import pme123.camundala.camunda.service.restService.Request.Auth.BasicAuth
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestBody.MultipartBody
import pme123.camundala.camunda.service.restService.RequestBody.Part.{FilePart, StringPart}
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.camunda.service.restService.{Request, RequestMethod, RequestPath, RestService}
import pme123.camundala.camunda.xml.{MergeResult, ValidateWarning, ValidateWarnings}
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn.{pathElemFromStr, _}
import pme123.camundala.model.deploy.{CamundaEndpoint, Deploy, Sensitive}
import zio.logging.Logging
import zio.{logging, _}

import scala.xml.XML

object httpDeployClient {
  type HttpDeployClient = Has[Service]

  trait Service {
    def deploy(deploy: Deploy): Task[Seq[DeployResult]]

    def deploy(request: DeployRequest): Task[DeployResult]

    def undeploy(bpmnId: BpmnId, endpoint: CamundaEndpoint): Task[Unit]

    def deployments(endpoint: CamundaEndpoint): Task[Seq[DeployResult]]
  }

  def deploy(deploy: Deploy): RIO[HttpDeployClient, Seq[DeployResult]] =
    ZIO.accessM(_.get.deploy(deploy))

  def deploy(request: DeployRequest): RIO[HttpDeployClient, DeployResult] =
    ZIO.accessM(_.get.deploy(request))

  def undeploy(bpmnId: BpmnId, endpoint: CamundaEndpoint): RIO[HttpDeployClient, Unit] =
    ZIO.accessM(_.get.undeploy(bpmnId, endpoint))

  def deployments(endpoint: CamundaEndpoint): RIO[HttpDeployClient, Seq[DeployResult]] =
    ZIO.accessM(_.get.deployments(endpoint))

  type HttpDeployClientDeps = RestService with AppConfig with BpmnService with Logging

  /**
    * http4s Implementation
    */
  lazy val live: RLayer[HttpDeployClientDeps, HttpDeployClient] =
    ZLayer.fromServices[restService.Service, appConfig.Service, bpmnService.Service, logging.Logger[String], Service] {
      (restServ, confService, bpmnServ, log) =>

        import DeployRequest._

        new Service {

          def deploy(deploy: Deploy): Task[Seq[DeployResult]] = {
            val endpoint = deploy.camundaEndpoint

            ZIO.foreach(deploy.bpmns) { bpmn =>
              deployBpmn(endpoint, bpmn)
            }
          }


          def deploy(request: DeployRequest): Task[DeployResult] =
            for {
              mergeResult <- mergeDeployFile(request.deployFile)
              deployResult <- mergeResult.maybeBpmn
                .map(deployBpmn(request.camundaEndpoint, _))
                .getOrElse(ZIO.fail(HttpDeployClientException(s"There is no BPMN for ${request.bpmnId}")))

            } yield deployResult

          private def deployBpmn(endpoint: CamundaEndpoint, bpmn: Bpmn) = {
            (for {
              mergeResult <- bpmnServ.mergeBpmn(bpmn.id)
              _ <- log.info(s"Deploy ${bpmn.id} to ${endpoint.url.value}")
              config <- confService.get()
              staticFiles <- ZIO.foreach(bpmn.staticFiles)(st =>
                propKeyFromStr(st.fileName.value)
                  .map(pk => FilePart(pk, st.fileName, StreamHelper(config.basePath).asString(st)))
              )
              bpmnName <- propKeyFromStr(bpmn.xml.fileName.value)
              body = MultipartBody(Set(
                StringPart(DeploymentName, bpmn.id.value),
                StringPart(EnableDuplicateFiltering, "false"),
                StringPart(DeployChangedOnly, "true"),
                StringPart(DeploymentSource, "Camundala Client"),
                FilePart(bpmnName, bpmn.xml.fileName, mergeResult.xmlElem.toString),
              ) ++ staticFiles)
              response <- restServ.call(Request(
                toHost(endpoint),
                RequestMethod.Post,
                Path("deployment", "create"),
                body = body
              ))
              deployResult <- JsonEnDecoders.toResult[DeployResult](toHost(endpoint), response)
            } yield
              deployResult.withWarnings(mergeResult.warnings))
              .catchAll(er => UIO(er.printStackTrace()) *> ZIO.fail(er match {
                case ex: HttpDeployClientException => ex
                case other => HttpDeployClientException(s"There is Problem deploying with ${bpmn.id} - see the stack trace", Some(other))
              }))
          }

          def undeploy(bpmnId: BpmnId, endpoint: CamundaEndpoint): Task[Unit] = {
            for {
              depls <- deploymentsForName(bpmnId.value, toHost(endpoint))
              r <- ZIO.foreach(depls)(deploy =>
                for {
                  _ <- log.info(s"Deployments for ${endpoint.url.value}")
                  deployId <- pathElemFromStr(deploy.id)
                  _ <- restServ.call(restService.Request(
                    toHost(endpoint),
                    RequestMethod.Delete,
                    Path("deployment", deployId),
                    Params(("cascade", "true"))
                  ))
                } yield ()
              )
            } yield r
          }.map(_ => ())
            .catchAll(er => UIO(er.printStackTrace()) *> ZIO.fail(er match {
              case ex: HttpDeployClientException => ex
              case _ => HttpDeployClientException(s"There is Problem delete the Deployments - see the stack trace")
            }))

          def deployments(endpoint: CamundaEndpoint): Task[Seq[DeployResult]] = {
            for {
              _ <- log.info(s"Deployments for ${endpoint.url.value}")
              response <- restServ.call(Request(
                toHost(endpoint),
                path = RequestPath.Path("deployment")
              ))
              deployResults <- JsonEnDecoders.toResult[Seq[DeployResult]](toHost(endpoint), response)
            } yield deployResults
          }.catchAll(er => UIO(er.printStackTrace()) *> ZIO.fail(er match {
            case ex: HttpDeployClientException => ex
            case _ => HttpDeployClientException(s"There is Problem with getting the Deployments - see the stack trace")
          }))

          private def mergeDeployFile(deployFile: DeployFile): Task[MergeResult] =
            for {
              xml <- ZIO.effect(XML.load(new ByteArrayInputStream(deployFile.file.toArray)))
              bpmnId <- bpmnIdFromFilePath(deployFile.filePath)
              mergeResult <- bpmnServ.mergeBpmn(bpmnId, xml)
              _ <- log.info(s"Merged BPMN:\n${mergeResult.xmlElem}")
            } yield mergeResult

          private def deploymentsForName(deploymentName: String, host: Host) = {
            for {
              _ <- log.info(s"Deployment $deploymentName for ${host.url.value}")
              response <- restServ.call(Request(
                host,
                path = Path("deployment"),
                queryParams = Params(("name", deploymentName))
              ))
              deployResults <- JsonEnDecoders.toResult[Seq[DeployResult]](host, response)
            } yield deployResults
          }

        }
    }

  private def toHost(endpoint: CamundaEndpoint) = {
    Host(endpoint.url, BasicAuth(endpoint.user, endpoint.password))
  }

  case class HttpDeployClientException(msg: String,
                                       override val cause: Option[Throwable] = None)
    extends CamundalaException

  implicit val deployResultDecoder: Decoder[DeployResult] = (c: HCursor) => for {
    id <- c.downField("id").as[String]
    name <- c.downField("name").as[Option[String]]
    deploymentTime <- c.downField("deploymentTime").as[String]
    source <- c.downField("source").as[Option[String]]
    tenantId <- c.downField("tenantId").as[Option[String]]
  } yield {
    DeployResult(id, name, deploymentTime, source, tenantId)
  }

  implicit val validationWarningsDecoder: Decoder[ValidateWarnings] = deriveDecoder[ValidateWarnings]
  implicit val validationWarningDecoder: Decoder[ValidateWarning] = deriveDecoder[ValidateWarning]

}
