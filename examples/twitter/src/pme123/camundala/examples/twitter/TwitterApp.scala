package pme123.camundala.examples.twitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.camunda.ZSpringApp
import pme123.camundala.cli.cliApp.CliApp
import pme123.camundala.cli.{ProjectInfo, cliApp}
import pme123.camundala.services.httpServer
import zio.ZIO
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
      _ <- httpServer.serve().fork
      _ <- registerBpmns(Set(bpmn))
      _ <- registerDeploys(Set(deploy))
      _ <- managedSpringApp(classOf[TwitterApp], args).useForever.fork
      _ <- runCli
    } yield ())
      // you have to provide all the layers here so all fibers have the same register
      .provideCustomLayer(layer)
      .fold(
        _ => 1,
        _ => 0
      )

  import pme123.camundala.camunda.DefaultLayers._

  private lazy val httpServerLayer = appConfigLayer ++ deploymentServiceLayer ++ logLayer("httpServer") >>> httpServer.live
  private lazy val cliLayer = (Console.live ++ deployRegisterLayer ++ deploymentServiceLayer) >>> cliApp.live

  protected def runCli: ZIO[CliApp with Console, Throwable, Nothing] =
    cliApp.run(projectInfo)

  private lazy val layer = cliLayer ++ httpServerLayer ++ bpmnServiceLayer ++ bpmnRegisterLayer ++ deployRegisterLayer

}
