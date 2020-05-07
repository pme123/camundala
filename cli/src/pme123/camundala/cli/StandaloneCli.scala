package pme123.camundala.cli

import pme123.camundala.app.appRunner
import pme123.camundala.camunda.DefaultLayers._
import zio.{Task, ZIO, ZLayer, console}
import zio.console.Console

object StandaloneCli
  extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      pi <- cliApp.camundala
      _ <- cliApp.run(pi.copy(name = "Standalone Console", sourceUrl = s"${pi.sourceUrl}/tree/master/cli"))
    } yield 0)
      .provideCustomLayer((Console.live ++ bpmnServiceLayer ++ deployRegisterLayer ++ deploymentServiceLayer ++ ZLayer.succeed(new appRunner.Service {
        override def run(): Task[Unit] = ???
      })) >>> cliApp.live)
      .catchAll(e => console.putStrLn(s"ERROR: $e").as(1))
}
