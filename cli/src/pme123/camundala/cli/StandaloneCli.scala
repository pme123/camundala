package pme123.camundala.cli

import zio.console.Console
import zio.{ZIO, console}

object StandaloneCli
  extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      pi <- cliApp.camundala
      _ <- cliApp.run(pi.copy(name = "Standalone Console", sourceUrl = s"${pi.sourceUrl}/tree/master/cli"), args)
    } yield 0)
      .catchAll(e => console.putStrLn(s"ERROR: $e").as(1))
      .provideCustomLayer(Console.live >>> cliApp.live)
}
