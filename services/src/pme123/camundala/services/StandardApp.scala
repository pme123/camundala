package pme123.camundala.services

import java.io.InputStreamReader

import javax.script.ScriptEngineManager
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.model.deploy.Deploys
import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.model.register.{bpmnRegister, deployRegister}
import pme123.camundala.services.httpServer.HttpServer
import zio._
import zio.logging.{Logging, log}
import zio.stm.TRef


object StandardApp {

  type StandardAppDeps = Logging with DeployRegister with BpmnRegister with HttpServer

  def layer(clazz: Class[_], bpmnModelsPath: String): ZLayer[StandardAppDeps, Nothing, AppRunner] =
    ZLayer.fromServicesM[logging.Logger[String], bpmnRegister.Service, deployRegister.Service, httpServer.Service, Any, Nothing, appRunner.Service](
      (log, bpmnRegService, deplRegService, httpServService) =>

        ZIO.foreach(0 to 1)(_ => TRef.make[Option[Fiber.Runtime[Throwable, Unit]]](None).commit).map { refs =>
          val camundaRef = refs.head
          val httpServerRef = refs.last

          new appRunner.Service {

            def start(): Task[Unit] = for {
              httpServerFiber <- httpServService.serve().fork
              _ <- httpServerRef.set(Some(httpServerFiber)).commit
              camundaFork <- managedSpringApp(clazz).useForever.fork.provideLayer(ZLayer.succeed(log))
              _ <- camundaRef.set(Some(camundaFork)).commit
            } yield ()

            def update(): Task[Unit] = for {
              _ <- log.info(s"Starting compile $bpmnModelsPath\nThis will take some time - Keep calm and enjoy a coffee;)")
              deploys <- readScript(bpmnModelsPath) // this takes a bit
              _ <- ZIO.foreach(deploys.value.flatMap(_.bpmns))(b => bpmnRegService.registerBpmn(b))
              _ <- ZIO.foreach(deploys.value)(d => deplRegService.registerDeploy(d))
              _ <- log.info(s"Registry  $bpmnModelsPath is updated\nThanks for your patience;)")
            } yield ()

            def stop(): Task[Unit] = for {
              maybeHttpFiber <- httpServerRef.get.commit
              httpFiber <- ZIO.fromOption(maybeHttpFiber).mapError(_ => new Exception("Service already down"))
              _ <- httpFiber.interrupt
              maybeCamundaFiber <- camundaRef.get.commit
              camFiber <- ZIO.fromOption(maybeCamundaFiber).mapError(_ => new Exception("Service already down"))
              _ <- camFiber.interrupt
            } yield ()

            def restart(): Task[Unit] =
              stop() *> start()
          }
        }
    )

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  def managedSpringApp(clazz: Class[_], args: List[String] = List.empty): ZManaged[Logging, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      log.info("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      log.info("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll((ex: Throwable) =>
          log.error(s"Problem shutting down the Spring Container.\n${ex.getMessage}", Cause.fail(ex))
        )
    )

  private[services] def readScript(bpmnModelsPath: String): Task[Deploys] = bpmnModels(bpmnModelsPath)
    .use { deploysReader =>
      for {
        e <- ZIO.effect(new ScriptEngineManager(getClass.getClassLoader).getEngineByName("scala"))
        scriptResult <- ZIO.effect(e.eval(deploysReader))
        deploys <- scriptResult match {
          case d: Deploys => UIO(d)
          case other => Task.fail(new Exception(s"Script did not contain Deploys: $other"))
        }
      } yield deploys
    }

  private def bpmnModels(bpmnModelsPath: String): Managed[Throwable, InputStreamReader] =
    ZManaged.make(ZIO.fromOption(Option(Thread.currentThread().getContextClassLoader.getResourceAsStream(bpmnModelsPath)))
      .map(i => new InputStreamReader(i)))(r =>
      ZIO.effect(r.close()).catchAll(e => UIO(e.printStackTrace()) *> UIO.unit)
    ).mapError(_ => NoResourceException(s"Resource $bpmnModelsPath could not be found"))


}
