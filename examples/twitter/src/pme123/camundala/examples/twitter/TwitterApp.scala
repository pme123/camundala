package pme123.camundala.examples.twitter

import java.nio.file.{Path, Paths}

import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda.CamundaLayers._
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.{CamundaLayers, ZSpringApp}
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.cli.{ProjectInfo, cliApp}
import pme123.camundala.config.ConfigLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.register.bpmnRegister.BpmnRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.services.StandardApp.StandardAppDeps
import pme123.camundala.services.httpServer.HttpServer
import pme123.camundala.services.{ServicesLayers, StandardApp, httpServer}
import zio._
import zio.clock.Clock
import zio.console.Console

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

  private[twitter] lazy val twitterApp: ZLayer[StandardAppDeps, Nothing, AppRunner] = StandardApp.layer(classOf[TwitterApp], bpmnModelsPath)

  private val bpmnModelsPath: Path = Paths.get(".", "examples", "twitter", "resources", "bpmnModels.sc")

  private[twitter] lazy val cliLayer = (Clock.live ++ Console.live ++ CamundaLayers.bpmnServiceLayer ++ ModelLayers.deployRegisterLayer ++ CamundaLayers.deploymentServiceLayer ++ ServicesLayers.dockerComposerLayer ++ twitterApp) >>> cliApp.live
  private[twitter] lazy val httpServerLayer = ConfigLayers.appConfigLayer ++ deploymentServiceLayer ++ ModelLayers.logLayer("httpServer") >>> httpServer.live
  private[twitter] lazy val appLayer: ZLayer[Any, Throwable, Console with HttpServer with BpmnService with BpmnRegister with DeployRegister] = Console.live ++ httpServerLayer ++ bpmnServiceLayer ++ ModelLayers.bpmnRegisterLayer ++ ModelLayers.deployRegisterLayer

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo)

  private[twitter] lazy val layer: ZLayer[Any, Throwable, CliApp] = appLayer >>> cliLayer

}
