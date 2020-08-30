package pme123.camundala.services

import java.io.InputStreamReader

import javax.script.ScriptEngineManager
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda.{CamundaApp, StreamHelper}
import pme123.camundala.config.appConfig
import pme123.camundala.config.appConfig.AppConfig
import pme123.camundala.model.bpmn.{CamundalaException, StaticFile}
import pme123.camundala.model.deploy.Deploys
import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.model.register.{bpmnRegister, deployRegister}
import pme123.camundala.services.httpServer.HttpServer
import zio._
import zio.logging.Logging
import zio.stm.TRef

import scala.jdk.CollectionConverters._

object StandardApp {

  type StandardAppDeps = Logging with AppConfig with DeployRegister with BpmnRegister with HttpServer

  def layer(clazz: Class[_], bpmnModel: StaticFile): ZLayer[StandardAppDeps, Nothing, AppRunner] =
    ZLayer.fromServicesM[logging.Logger[String], appConfig.Service, bpmnRegister.Service, deployRegister.Service, httpServer.Service, Any, Nothing, appRunner.Service](
      (log, configService, bpmnRegService, deplRegService, httpServService) =>

        ZIO.foreach(0 to 1)(_ => TRef.make[Option[Fiber.Runtime[Throwable, Unit]]](None).commit).map { refs =>
          val camundaRef = refs.head
          val httpServerRef = refs.last

          def readScript(): Task[Deploys] = bpmnModels().use { zDeploysReader =>
            val manager = new ScriptEngineManager(getClass.getClassLoader)
            for {
              reader <- zDeploysReader
              e <- ZIO.effect(manager.getEngineByExtension("scala"))
              _ <- log.info(s"Script Engine: $e from: ${manager.getEngineFactories.asScala.map(f => s"names: ${f.getEngineName} - extensions: ${f.getExtensions}")}")
              scriptResult <- ZIO.effect(e.eval(reader))
              deploys <- scriptResult match {
                case d: Deploys => UIO(d)
                case other => Task.fail(new Exception(s"Script did not contain Deploys: $other"))
              }
            } yield deploys
          }

          def bpmnModels(): ZManaged[Any, NoResourceException, ZIO[Any, Throwable, InputStreamReader]] =
            ZManaged.makeEffect(configService.get()
              .flatMap(config => StreamHelper(config.basePath).inputStreamM(bpmnModel))
              .map(i => new InputStreamReader(i)))(r =>
              r.map(_.close()).catchAll(e => UIO(e.printStackTrace()).unit)
            ).orElseFail(NoResourceException(s"Resource ${bpmnModel.pathWithName} could not be found"))


          new appRunner.Service {

            def start(): Task[Unit] = for {
              httpServerFiber <- httpServService.serve().fork
              _ <- httpServerRef.set(Some(httpServerFiber)).commit
              camundaFork <- CamundaApp.managedSpringApp(clazz).useForever.fork.provideLayer(ZLayer.succeed(log))
              _ <- camundaRef.set(Some(camundaFork)).commit
            } yield ()

            def update(): Task[Unit] = for {
              _ <- log.info(s"Starting compile ${bpmnModel.pathWithName}\nThis will take some time - Keep calm and enjoy a coffee;)")
              deploys <- readScript() // this takes a bit
              _ <- ZIO.foreach(deploys.value.flatMap(_.bpmns))(b => bpmnRegService.registerBpmn(b))
              _ <- ZIO.foreach(deploys.value)(d => deplRegService.registerDeploy(d))
              _ <- log.info(s"Registry  ${bpmnModel.pathWithName} is updated\nThanks for your patience;)")
            } yield ()

            def stop(): Task[Unit] = for {
              maybeHttpFiber <- httpServerRef.get.commit
              httpFiber <- ZIO.fromOption(maybeHttpFiber).orElseFail(StandardAppException("Service already down"))
              _ <- httpFiber.interrupt
              maybeCamundaFiber <- camundaRef.get.commit
              camFiber <- ZIO.fromOption(maybeCamundaFiber).orElseFail(StandardAppException("Service already down"))
              _ <- camFiber.interrupt
            } yield ()

            def restart(): Task[Unit] =
              stop() *> start()
          }
        }
    )

  case class StandardAppException(msg: String,
                                  override val cause: Option[Throwable] = None)
    extends CamundalaException

}
