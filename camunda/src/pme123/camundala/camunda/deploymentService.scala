package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings}
import pme123.camundala.model.CamundalaException
import zio._
import zio.macros.accessible

import scala.xml.XML

/**
  * Deploys a Project
  * at the moment it only deploys a BPMN to Camunda.
  * The goal is to adjust the deployment with all needed files.
  */
@accessible
object deploymentService {

  type DeploymentService = Has[Service]

  trait Service {
    def deploy(request: DeployRequest): Task[DeployResult]
  }

  type DeploymentServiceDeps = BpmnService with ProcessEngineService

  lazy val live: RLayer[DeploymentServiceDeps, DeploymentService] =
    ZLayer.fromServices[bpmnService.Service, processEngineService.Service, Service] {
      (bpmnServ, processEngineService) =>
        new Service {
          private def mergeDeployFiles(deployFiles: Set[DeployFile]): Task[List[MergeResult]] =
            ZIO.foreach(deployFiles)(mergeDeployFile)

          private def mergeDeployFile(deployFile: DeployFile): Task[MergeResult] =
            for {
              xml <- ZIO.effect(XML.load(new ByteArrayInputStream(deployFile.file.toArray)))
              mergeResult <- bpmnServ.mergeBpmn(deployFile.filename, xml)
            } yield mergeResult

          def deploy(request: DeployRequest): Task[DeployResult] =
            for {
              mergeResults <- mergeDeployFiles(request.deployFiles)
              deployment <- processEngineService.deploy(request, mergeResults)
              deployResult = DeployResult(deployment.getId, deployment.getName,
                deployment.getDeploymentTime.toString,
                Option(deployment.getSource),
                Option(deployment.getTenantId),
                mergeResults.map(_.warnings).foldLeft(ValidateWarnings.none)(_ ++ _)
              )
            } yield deployResult
        }
    }

  case class DeployRequest(name: Option[String] = None,
                           enableDuplicateFilterung: Boolean = false,
                           deployChangedOnly: Boolean = false,
                           source: Option[String] = None,
                           tenantId: Option[String] = None,
                           deployFiles: Set[DeployFile] = Set.empty)

  object DeployRequest {
    val DEPLOYMENT_NAME = "deployment-name"
    val ENABLE_DUPLICATE_FILTERING = "enable-duplicate-filtering"
    val DEPLOY_CHANGED_ONLY = "deploy-changed-only"
    val DEPLOYMENT_SOURCE = "deployment-source"
    val TENANT_ID = "tenant-id"

    val RESERVED_KEYWORDS = Set(
      DEPLOYMENT_NAME,
      ENABLE_DUPLICATE_FILTERING,
      DEPLOY_CHANGED_ONLY,
      DEPLOYMENT_SOURCE,
      TENANT_ID
    )
  }

  case class DeployFile(filename: String, file: Vector[Byte])

  case class DeployResult(id: String,
                          name: String,
                          deploymentTime: String,
                          source: Option[String] = None,
                          tenantId: Option[String] = None,
                          validateWarnings: ValidateWarnings
                         )

  case class DeploymentException(msg: String) extends CamundalaException

}