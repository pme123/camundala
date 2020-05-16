package pme123.camundala.services

import pme123.camundala.app.sttpBackend
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.deploy.DockerConfig
import pme123.camundala.services.dockerComposer.DockerComposer
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio.{App, ULayer, ZIO, console}

object DockerUp extends App {
  import DockerRunner._

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    runDocker(for {
      _ <- dockerComposer.runDockerUp(DockerConfig())
    } yield 0)
}

object DockerDown extends App {
  import DockerRunner._

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    runDocker(for {
      _ <- dockerComposer.runDockerDown(DockerConfig())
    } yield 0)
}

object DockerRunner {
  def runDocker(composeRun: ZIO[DockerComposer with Clock, Throwable, Int]): ZIO[zio.ZEnv, Nothing, Int] = {
    composeRun
      .provideCustomLayer(sttpBackend.sttpBackendLayer ++ ModelLayers.logLayer("DockerRunner") >>> dockerComposer.live)
      .catchAll(e => console.putStrLn(s"ERROR: $e").as(1))
  }

  def logLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )
}