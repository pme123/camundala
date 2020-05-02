package pme123.camundala.cli

import cats.effect.ExitCode
import com.monovore.decline._
import com.monovore.decline.effect.CommandIOApp
import pme123.camundala.cli.ProjectInfo._
import zio._
import zio.console.{Console, putStr => p, putStrLn => pl}
import zio.interop.catz._

import scala.io.{BufferedSource, Source}

object cliApp {

  type CliApp = Has[Service]

  trait Service {
    def run(projectInfo: ProjectInfo, args: List[String]): ZIO[Console, Throwable, Nothing]
  }

  def run(projectInfo: ProjectInfo, args: List[String]): ZIO[CliApp with Console, Throwable, Nothing] =
    ZIO.accessM(_.get.run(projectInfo, args))

  lazy val live: ULayer[CliApp] =
    ZLayer.succeed {
      (projectInfo: ProjectInfo, args: List[String]) =>
        intro *>
          printProject(projectInfo) *>
          (for {
            input <- zio.console.getStrLn
            _ <- CommandIOApp.run(command, input.split(" ").toList)
          } yield ())
            .tapError(e => zio.console.putStrLn(s"Error: $e"))
            .forever
    }
  private val width = 84
  private val versionFile: zio.Managed[Throwable, BufferedSource] = zio.Managed.make(ZIO.effect(Source.fromFile("./version")))(s => ZIO.succeed(s.close()))

  private[cli] val camundala: Task[ProjectInfo] =
    for {
      version <- versionFile.use(vf => ZIO.effect(vf.getLines().next()))
    } yield ProjectInfo("camundala", "pme123", version, "https://github.com/pme123/camundala")

  private val intro =
    for {
      - <- p(scala.Console.MAGENTA)
      _ <- pl("*" * width)
      - <- p(scala.Console.BLUE)
      _ <- p(
        """|     _____
           |  __|___  |__  ____    ____    __  __   _  ____   _  _____   ____    ____    ____
           | |   ___|    ||    \  |    \  /  ||  | | ||    \ | ||     \ |    \  |    |  |    \
           | |   |__     ||     \ |     \/   ||  |_| ||     \| ||      \|     \ |    |_ |     \
           | |______|  __||__|\__\|__/\__/|__||______||__/\____||______/|__|\__\|______||__|\__\
           |    |_____|    Doing Camunda with Scala""".stripMargin)
      - <- p(scala.Console.MAGENTA)
      pi <- camundala
      _ <- line(versionLabel, pi.version, leftAligned = false)
      _ <- line(licenseLabel, pi.license, leftAligned = false)
      _ <- line("", pi.sourceUrl, leftAligned = false)
      _ <- pl("")
      _ <- pl("*" * width)
      _ <- pl(" For Help type '--help'")
      - <- p(scala.Console.RESET)
    } yield ()

  private def printProject(projectInfo: ProjectInfo) = {
    for {
      - <- p(scala.Console.BLUE)
      _ <- line(nameLabel, projectInfo.name)
      _ <- line(orgLabel, projectInfo.org)
      _ <- line(versionLabel, projectInfo.version)
      _ <- line(licenseLabel, projectInfo.license)
      _ <- line(sourceUrlLabel, projectInfo.sourceUrl)
      _ <- pl("")
      _ <- pl("-" * width)
      - <- p(scala.Console.RESET)
    } yield ()
  }

  private def line(label: String, value: String, leftAligned: Boolean = true) =
    for {
      _ <- pl("")
      _ <- if (leftAligned)
        p(" ")
      else
        p(" " * (width - (value.length + label.length)))
      _ <- p(s"$label$value")
    } yield ()

  private val opts: Opts[Task[ExitCode]] = {
    val toGreetOpt = Opts.argument[String]("to-greet")
    toGreetOpt.map { toGreet =>
      Task.succeed(println(s"Hello $toGreet")).as(ExitCode.Success)
    }
  }
  private val command = Command[Task[ExitCode]]("", "Pure Hello World with Decline")(opts)


}