package pme123.camundala.services

import pme123.camundala.app.sttpBackend
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.deploy.DockerConfig
import pme123.camundala.services.dockerComposer.DockerComposer
import zio.clock.Clock
import zio.console.Console
import zio.logging.Logging
import zio._

object DockerUp extends App {
  import DockerRunner._

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    runDocker(for {
      _ <- dockerComposer.runDockerUp(DockerConfig())
    } yield ExitCode.success)
}

object DockerDown extends App {
  import DockerRunner._

  override def run(args: List[String]): ZIO[zio.ZEnv, Nothing, ExitCode] =
    runDocker(for {
      _ <- dockerComposer.runDockerDown(DockerConfig())
    } yield ExitCode.success)
}

object DockerRunner {
  def runDocker(composeRun: ZIO[DockerComposer with Clock, Throwable, ExitCode]): ZIO[zio.ZEnv, Nothing, ExitCode] = {
    composeRun
      .provideCustomLayer(sttpBackend.sttpBackendLayer ++ ModelLayers.logLayer("DockerRunner") >>> dockerComposer.live)
      .catchAll(e => console.putStrLn(s"ERROR: $e").as(ExitCode.failure))
  }

  def logLayer(loggerName: String): ULayer[Logging] = (Console.live ++ Clock.live) >>> Logging.console(
    format = (_, logEntry) => logEntry,
    rootLoggerName = Some(loggerName)
  )
}
