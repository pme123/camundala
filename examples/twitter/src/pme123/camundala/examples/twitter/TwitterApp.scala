package pme123.camundala.examples.twitter

import java.io.InputStreamReader
import java.nio.file.{Path, Paths}

import javax.script.ScriptEngineManager
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda.CamundaLayers._
import pme123.camundala.camunda.{CamundaLayers, ZSpringApp}
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.cli.{ProjectInfo, cliApp}
import pme123.camundala.config.ConfigLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.bpmn.bpmnRegister
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.deploy.deployRegister.DeployRegister
import pme123.camundala.model.deploy.{Deploys, deployRegister}
import pme123.camundala.services.httpServer.HttpServer
import pme123.camundala.services.{ServicesLayers, httpServer}
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.stm.TRef

import scala.io.Source

@SpringBootApplication
class TwitterApp

object TwitterApp extends ZSpringApp {
  val projectInfo: ProjectInfo =
    ProjectInfo(
      "Twitter Camundala Demo App",
      camundala.BuildInfo.organization,
      camundala.BuildInfo.version,
      s"${camundala.BuildInfo.url}/tree/master/examples/twitter",
      camundala.BuildInfo.license
    )

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      _ <- runCli
    } yield ())
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(layer)
      .fold(
        _ => 1,
        _ => 0
      )

  type TwitterAppDeps = Console with DeployRegister with BpmnRegister with HttpServer
  private lazy val twitterApp: ZLayer[TwitterAppDeps, Nothing, AppRunner] =
    ZLayer.fromServicesM[Console.Service, bpmnRegister.Service, deployRegister.Service, httpServer.Service, Any, Nothing, appRunner.Service](
      (console, bpmnRegService, deplRegService, httpServService) =>

        ZIO.foreach(0 to 1)(_ => TRef.make[Option[Fiber.Runtime[Throwable, Unit]]](None).commit).map {
          case camundaRef :: httpServerRef :: _ =>

            new appRunner.Service {

              def start(): Task[Unit] = for {
                httpServerFiber <- httpServService.serve().fork
                _ <- httpServerRef.set(Some(httpServerFiber)).commit
                _ <- update()
                camundaFork <- managedSpringApp(classOf[TwitterApp]).useForever.fork.provideLayer(ZLayer.succeed(console))
                _ <- camundaRef.set(Some(camundaFork)).commit
              } yield ()

              def update(): Task[Unit] = for {
                deploys <- readScript
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

  private val bpmnModelsPath: Path = Paths.get(".", "examples", "twitter", "resources", "bpmnModels.sc")
  private lazy val readScript: Task[Deploys] = bpmnModels
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

  private lazy val bpmnModels: Managed[Throwable, InputStreamReader] = ZManaged.make(ZIO.effect(Source.fromFile(bpmnModelsPath.toFile).reader()))(r =>
    ZIO.effect(r.close()).catchAll(e => UIO(e.printStackTrace()) *> UIO.unit))

  private lazy val cliLayer = (Clock.live ++ Console.live ++ CamundaLayers.bpmnServiceLayer ++ ModelLayers.deployRegisterLayer ++ CamundaLayers.deploymentServiceLayer ++ ServicesLayers.dockerComposerLayer ++ twitterApp) >>> cliApp.live
  private lazy val httpServerLayer = ConfigLayers.appConfigLayer ++ deploymentServiceLayer ++ ModelLayers.logLayer("httpServer") >>> httpServer.live
  private lazy val appLayer = Console.live ++ httpServerLayer ++ bpmnServiceLayer ++ ModelLayers.bpmnRegisterLayer ++ ModelLayers.deployRegisterLayer

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo)

  private lazy val layer: ZLayer[Any, Throwable, CliApp] = appLayer >>> cliLayer

}
