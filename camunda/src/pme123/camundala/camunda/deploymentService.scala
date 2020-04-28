package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import org.camunda.bpm.engine.rest.util.EngineUtil
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.xml.{ValidateWarnings, XMergeResult}
import pme123.camundala.model.CamundalaException
import zio._

import scala.xml.XML

/**
  * Deploys a Project
  * at the moment it only deploys a BPMN to Camunda.
  * The goal is to adjust the deployment with all needed files.
  */
object deploymentService {

  type DeploymentService = Has[Service]

  trait Service {
    def deploy(request: DeployRequest): Task[DeployResult]
  }

  def deploy(request: DeployRequest): RIO[DeploymentService, DeployResult] =
    ZIO.accessM(_.get.deploy(request))

  type DeploymentServiceDeps = BpmnService

  lazy val live: RLayer[DeploymentServiceDeps, DeploymentService] =
    ZLayer.fromService[bpmnService.Service, Service] {
      bpmnServ =>
        new Service {
          private lazy val processEngine = EngineUtil.lookupProcessEngine(null)

          private def mergeDeployFiles(deployFiles: Set[DeployFile]): Task[List[(DeployFile, XMergeResult)]] =
            Task.collectAll(deployFiles.map(mergeDeployFile))

          private def mergeDeployFile(deployFile: DeployFile): Task[(DeployFile, XMergeResult)] =
            for {
              xml <- ZIO.effect(XML.load(new ByteArrayInputStream(deployFile.file.toArray)))
              bpmn <- bpmnServ.mergeBpmn(deployFile.filename, xml)
            } yield deployFile -> bpmn

          def deploy(request: DeployRequest): Task[DeployResult] =
            for {
              models <- mergeDeployFiles(request.deployFiles)
              name <- ZIO.fromOption(request.name)
                .catchAll(_ => ZIO.fail(DeploymentException("The deployment name must be set.")))
              builder <- ZIO.effect(
                processEngine.getRepositoryService.createDeployment
                  .name(name)
                  .enableDuplicateFiltering(request.enableDuplicateFilterung)
              )
              b1 <- ZIO.succeed(request.source.map(builder.source).getOrElse(builder))
              b2 <- ZIO.succeed(request.tenantId.map(b1.tenantId).getOrElse(b1))
              b3 <- ZIO.effect(
                models.foldLeft(b2) { case (builder, (df, mr)) =>
                  builder.addInputStream(df.filename,
                    new ByteArrayInputStream(mr.xmlNode.toString.getBytes)
                  )
                })
              deployment <- ZIO.effect(b3.deploy())
              deployResult = DeployResult(deployment.getId, deployment.getName,
                deployment.getDeploymentTime.toString,
                Option(deployment.getSource),
                Option(deployment.getTenantId),
                models.map(_._2.warnings).foldLeft(ValidateWarnings.none)(_ ++ _)
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