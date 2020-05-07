package pme123.camundala.camunda

import java.io.ByteArrayInputStream

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.repository.{Deployment, DeploymentBuilder}
import pme123.camundala.camunda.xml.MergeResult
import pme123.camundala.model.bpmn.{CamundalaException, StaticFile}
import zio._
import scala.jdk.CollectionConverters._

/**
  * Wraps functionality that needs the Camunda ProcessEngine (Camunda must run).
  *
  */
object processEngineService {

  type ProcessEngineService = Has[Service]

  trait Service {
    def deploy(deployRequest: DeployRequest, mergeResults: Seq[MergeResult]): Task[Deployment]

    def deployments(): Task[Seq[Deployment]]
  }

  def deploy(deployRequest: DeployRequest, mergeResults: Seq[MergeResult]): RIO[ProcessEngineService, Deployment] =
    ZIO.accessM(_.get.deploy(deployRequest, mergeResults))

  def deployments(): RIO[ProcessEngineService, Seq[Deployment]] =
    ZIO.accessM(_.get.deployments())

  import CamundaExtensions._

  type ProcessEngineServiceDeps = Has[() => ProcessEngine]

  lazy val live: RLayer[ProcessEngineServiceDeps, ProcessEngineService] =
    ZLayer.fromService[() => ProcessEngine, Service] { processEngine =>
      new Service {
        def deploy(request: DeployRequest, mergeResults: Seq[MergeResult]): Task[Deployment] =
          for {
            name <- ZIO.fromOption(request.name)
              .catchAll(_ => ZIO.fail(ProcessEngineException("The deployment name must be set.")))
            builder <-
              ZIO.effect(
                processEngine().getRepositoryService.createDeployment
                  .name(name)
                  .obtValue((b, v: String) => b.source(v), request.source)
                  .obtValue((b, v: String) => b.tenantId(v), request.tenantId)
                  .enableDuplicateFiltering(request.enableDuplicateFilterung)
                  .listValue((b, v: MergeResult) => b.addInputStream(v.fileName, new ByteArrayInputStream(v.xmlElem.toString.getBytes)), mergeResults)
                  .listValue((b, v: StaticFile) => b.addInputStream(v.fileName, v.inputStream), mergeResults.flatMap(_.maybeBpmn).flatMap(_.staticFiles))
              )
            deployment <- ZIO.effect(builder.deploy())
          } yield deployment

        def deployments(): Task[Seq[Deployment]] =
          ZIO.effect(
            processEngine().getRepositoryService.createDeploymentQuery()
              .list()
              .asScala
              .toSeq
          )

      }

    }

  case class ProcessEngineException(msg: String) extends CamundalaException

}

object CamundaExtensions {

  implicit class DeploymentBuilderExt(builder: DeploymentBuilder) {

    def obtValue[T](funct: (DeploymentBuilder, T) => DeploymentBuilder, maybeValue: Option[T]): DeploymentBuilder =
      maybeValue.map(funct(builder, _)).getOrElse(builder)

    def listValue[T](funct: (DeploymentBuilder, T) => DeploymentBuilder, values: Seq[T]): DeploymentBuilder =
      values.foldLeft(builder) { case (b, value) => funct(b, value) }
  }

}