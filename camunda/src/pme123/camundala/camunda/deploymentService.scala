package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.processEngineService.ProcessEngineService
import pme123.camundala.camunda.xml.{MergeResult, ValidateWarnings}
import pme123.camundala.model.bpmn._
import zio._
import zio.logging.{Logger, Logging}

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


  type DeploymentServiceDeps = BpmnService with ProcessEngineService with Logging

  lazy val live: RLayer[DeploymentServiceDeps, DeploymentService] =
    ZLayer.fromServices[bpmnService.Service, processEngineService.Service, Logger[String], Service] {
      (bpmnServ, processEngineService, log) =>
        new Service {
          private def mergeDeployFiles(deployFiles: Set[DeployFile]): Task[List[MergeResult]] =
            ZIO.foreach(deployFiles)(mergeDeployFile)

          private def mergeDeployFile(deployFile: DeployFile): Task[MergeResult] =
            for {
              xml <- ZIO.effect(XML.load(new ByteArrayInputStream(deployFile.file.toArray)))
              bpmnId <- bpmnIdFromFilePath(deployFile.filePath)
              mergeResult <- bpmnServ.mergeBpmn(bpmnId, xml)
              _ <- log.info(s"Merged BPMN:\n${mergeResult.xmlElem}")
            } yield mergeResult

          def deploy(request: DeployRequest): Task[DeployResult] =
            for {
              mergeResults <- mergeDeployFiles(request.deployFiles)
              deployment <- processEngineService.deploy(request, mergeResults)
              deployResult = DeployResult(deployment.getId, Some(deployment.getName),
                deployment.getDeploymentTime.toString,
                Option(deployment.getSource),
                Option(deployment.getTenantId),
                mergeResults.map(_.warnings).foldLeft(ValidateWarnings.none)(_ ++ _)
              )
            } yield deployResult
        }
    }
}