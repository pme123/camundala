package pme123.camundala.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.repository.{Deployment, DeploymentBuilder}
import pme123.camundala.camunda.StreamHelper._
import pme123.camundala.camunda.xml.MergeResult
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn.{CamundalaException, StaticFile}
import pme123.camundala.model.deploy.DeployId
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

    def undeploy(deployId: DeployId): Task[Unit]

    def deployments(): Task[Seq[Deployment]]
  }

  def deploy(deployRequest: DeployRequest, mergeResults: Seq[MergeResult]): RIO[ProcessEngineService, Deployment] =
    ZIO.accessM(_.get.deploy(deployRequest, mergeResults))

  def undeploy(deployId: DeployId): RIO[ProcessEngineService, Unit] =
    ZIO.accessM(_.get.undeploy(deployId))

  def deployments(): RIO[ProcessEngineService, Seq[Deployment]] =
    ZIO.accessM(_.get.deployments())

  import CamundaExtensions._

  type ProcessEngineServiceDeps = Has[() => ProcessEngine] with AppConfig

  lazy val live: RLayer[ProcessEngineServiceDeps, ProcessEngineService] =
    ZLayer.fromServices[() => ProcessEngine, appConfig.Service, Service] { (processEngine, configService) =>
      new Service {
        private lazy val repoServiceEffect = (for {
          pe <- ZIO.fromOption(Option(processEngine()))
          rs <- ZIO.effect(pe.getRepositoryService)
        } yield rs)
          .mapError(_ => ProcessEngineException("Camunda Process Engine is not running. Did you start Camunda?"))

        def deploy(request: DeployRequest, mergeResults: Seq[MergeResult]): Task[Deployment] =
          for {
            repoService <- repoServiceEffect
            config <- configService.get()
            builder <-
              ZIO.effect(
                repoService.createDeployment
                  .name(request.bpmnId.value)
                  .obtValue((b, v: String) => b.source(v), request.source)
                  .obtValue((b, v: String) => b.tenantId(v), request.tenantId)
                  .enableDuplicateFiltering(request.enableDuplicateFilterung)
                  .listValue((b, v: MergeResult) => b.addInputStream(v.fileName.value, StreamHelper(config.basePath).inputStream(v.xmlElem)), mergeResults)
                  .listValue((b, v: StaticFile) => b.addInputStream(v.fileName.value, StreamHelper(config.basePath).inputStream(v)), mergeResults.flatMap(_.maybeBpmn).flatMap(_.staticFiles))
              )
            deployment <- ZIO.effect(builder.deploy())
          } yield deployment

        def undeploy(deployId: DeployId): Task[Unit] =
          repoServiceEffect.map(repoService =>
            repoService.createDeploymentQuery()
              .deploymentName(deployId.value)
              .list()
              .asScala
              .foreach(d => repoService.deleteDeployment(d.getId, true))
          )

        def deployments(): Task[Seq[Deployment]] =
          repoServiceEffect.map(repoService =>
            repoService.createDeploymentQuery()
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