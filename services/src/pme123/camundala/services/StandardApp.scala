package pme123.camundala.services

import java.io.InputStreamReader
import java.nio.file.Path

import javax.script.ScriptEngineManager
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.model.bpmn.bpmnRegister
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.deploy.deployRegister.DeployRegister
import pme123.camundala.model.deploy.{Deploys, deployRegister}
import pme123.camundala.services.httpServer.HttpServer
import zio.console.Console
import zio.stm.TRef
import zio._

import scala.io.Source

object StandardApp {

  type StandardAppDeps = Console with DeployRegister with BpmnRegister with HttpServer

  def layer(clazz: Class[_], bpmnModelsPath: Path): ZLayer[StandardAppDeps, Nothing, AppRunner] =
    ZLayer.fromServicesM[Console.Service, bpmnRegister.Service, deployRegister.Service, httpServer.Service, Any, Nothing, appRunner.Service](
      (console, bpmnRegService, deplRegService, httpServService) =>

        ZIO.foreach(0 to 1)(_ => TRef.make[Option[Fiber.Runtime[Throwable, Unit]]](None).commit).map { refs =>
          val camundaRef = refs.head
          val httpServerRef = refs.last

          new appRunner.Service {

            def start(): Task[Unit] = for {
              httpServerFiber <- httpServService.serve().fork
              _ <- httpServerRef.set(Some(httpServerFiber)).commit
              _ <- update()
              camundaFork <- managedSpringApp(clazz).useForever.fork.provideLayer(ZLayer.succeed(console))
              _ <- camundaRef.set(Some(camundaFork)).commit
            } yield ()

            def update(): Task[Unit] = for {
              deploys <- readScript(bpmnModelsPath)
              _ <- ZIO.foreach(deploys.value.flatMap(_.bpmns))(b => bpmnRegService.registerBpmn(b))
              _ <- ZIO.foreach(deploys.value)(d => deplRegService.registerDeploy(d))
            } yield ()

            def stop(): Task[Unit] = (for {
              maybeHttpFiber <- httpServerRef.get.commit
              httpFiber <- ZIO.fromOption(maybeHttpFiber).mapError(_ => new Exception("Service already down"))
              _ <- httpFiber.interrupt
              maybeCamundaFiber <- camundaRef.get.commit
              camFiber <- ZIO.fromOption(maybeCamundaFiber).mapError(_ => new Exception("Service already down"))
              _ <- camFiber.interrupt
            } yield ())

            def restart(): Task[Unit] =
              stop() *> start()
          }
        }
    )

  /**
    * create SpringApplication as a ZManaged Resource.
    */
  def managedSpringApp(clazz: Class[_], args: List[String] = List.empty): ZManaged[Console, Throwable, ConfigurableApplicationContext] =
    ZManaged.make(
      console.putStrLn("Starting Spring Container...") *>
        ZIO.effect(
          SpringApplication.run(clazz, args: _*)
        )
    )(ctx =>
      console.putStrLn("Spring Container Stopping...") *>
        ZIO.effect(
          if (ctx.isActive)
            SpringApplication.exit(ctx)
        ).catchAll(ex =>
          console.putStrLn(s"Problem shutting down the Spring Container.\n${ex.getMessage}")
        )
    )

  private def readScript(bpmnModelsPath: Path): Task[Deploys] = bpmnModels(bpmnModelsPath)
    .use { deploysReader =>
      for {
        e <- ZIO.effect(new ScriptEngineManager().getEngineByName("scala"))
        scriptResult <- ZIO.effect(e.eval(deploysReader))
        deploys <- scriptResult match {
          case d: Deploys => UIO(d)
          case other => Task.fail(new Exception(s"Script did not contain Deploys: $other"))
        }
      } yield deploys
    }

  private def bpmnModels(bpmnModelsPath: Path): Managed[Throwable, InputStreamReader] = ZManaged.make(ZIO.effect(Source.fromFile(bpmnModelsPath.toFile).reader()))(r =>
    ZIO.effect(r.close()).catchAll(e => UIO(e.printStackTrace()) *> UIO.unit))
}
