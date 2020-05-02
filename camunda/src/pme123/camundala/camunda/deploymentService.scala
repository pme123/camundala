package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings}
import pme123.camundala.model.bpmn.Bpmn
import zio._
import zio.macros.accessible

import scala.collection.immutable.HashSet
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

    def deploy(bpmn: Bpmn): Task[DeployResult]

    def deployments(): Task[Seq[DeployResult]]
  }

  def deploy(request: DeployRequest): RIO[DeploymentService, DeployResult] =
    ZIO.accessM(_.get.deploy(request))

  def deploy(bpmn: Bpmn): RIO[DeploymentService, DeployResult] =
    ZIO.accessM(_.get.deploy(bpmn))

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

          def deploy(bpmn: Bpmn): Task[DeployResult] =
            for {
              mergeResult <- bpmnServ.mergeBpmn(bpmn.id)
              xml <- bpmn.xml.xml
              deployment <- processEngineService.deploy(DeployRequest(Some(bpmn.id),
                source = Some("Camundala Deployer"),
                deployFiles = HashSet(DeployFile(bpmn.xml.fileName, xml.toString().getBytes().toVector))), Seq(mergeResult))
            } yield DeployResult(deployment.getId, deployment.getName,
              deployment.getDeploymentTime.toString,
              Option(deployment.getSource),
              Option(deployment.getTenantId),
              mergeResult.warnings
            )

          def deployments(): Task[Seq[DeployResult]] =
            for {
              depls <- processEngineService.deployments()
              result = depls.map(deployment =>
                DeployResult(deployment.getId, deployment.getName,
                  deployment.getDeploymentTime.toString,
                  Option(deployment.getSource),
                  Option(deployment.getTenantId)
                )
              )
            } yield result
        }
    }


}