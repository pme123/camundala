package pme123.camundala.examples.twitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda.DefaultLayers._
import pme123.camundala.camunda.ZSpringApp
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.cli.{ProjectInfo, cliApp}
import pme123.camundala.model.bpmn.bpmnRegister.BpmnRegister
import pme123.camundala.model.bpmn.{Bpmn, bpmnRegister}
import pme123.camundala.model.deploy.deployRegister.DeployRegister
import pme123.camundala.model.deploy.{Deploy, deployRegister}
import pme123.camundala.services.httpServer
import pme123.camundala.services.httpServer.HttpServer
import zio._
import zio.console.Console

@SpringBootApplication
//@EnableProcessApplication
class TwitterApp

object TwitterApp extends ZSpringApp {
  val projectInfo: ProjectInfo =
    ProjectInfo(
      "Twitter Camundala Demo App",
      "pme123",
      "0.0.1",
      "https://github.com/pme123/camundala/tree/master/examples/twitter"
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

  protected def registerBpmns(bpmns: Set[Bpmn]): URIO[BpmnRegister, List[Unit]] =
    ZIO.foreach(bpmns.toSeq)(b => bpmnRegister.registerBpmn(b))

  protected def registerDeploys(deploys: Set[Deploy]): URIO[DeployRegister, List[Unit]] =
    ZIO.foreach(deploys.toSeq)(d => deployRegister.registerDeploy(d))

  type TwitterAppDeps = Console with DeployRegister with BpmnRegister with HttpServer

  protected lazy val twitterApp: RLayer[TwitterAppDeps, AppRunner] =
    ZLayer.fromServices[Console.Service, bpmnRegister.Service, deployRegister.Service, httpServer.Service, appRunner.Service] {
      (console, bpmnRegService, deplRegService, httpServService) =>
        implicit val c: Console.Service = console
        new appRunner.Service {
          def run(): Task[Unit] = (for {
            _ <- httpServService.serve().fork
            _ <- ZIO.foreach(Set(bpmn))(b => bpmnRegService.registerBpmn(b))
            _ <- ZIO.foreach(Seq(deploy))(d => deplRegService.registerDeploy(d))
            _ <- managedSpringApp(classOf[TwitterApp]).useForever
          } yield ())
          def update(): Task[Unit] = (for {
            _ <- ZIO.foreach(Set(bpmn))(b => bpmnRegService.registerBpmn(b))
            _ <- ZIO.foreach(Seq(deploy))(d => deplRegService.registerDeploy(d))
          } yield ())
        }
    }

  private lazy val cliLayer = (Console.live ++ bpmnServiceLayer ++ deployRegisterLayer ++ deploymentServiceLayer ++ twitterApp) >>> cliApp.live
  private lazy val httpServerLayer = appConfigLayer ++ deploymentServiceLayer ++ logLayer("httpServer") >>> httpServer.live
  private lazy val appLayer = Console.live ++ httpServerLayer ++ bpmnServiceLayer ++ bpmnRegisterLayer ++ deployRegisterLayer

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo)

  private lazy val layer: ZLayer[Any, Throwable, CliApp] = appLayer >>> cliLayer

}
