package pme123.camundala.cli

import cats.effect.{ExitCode, IO}
import cats.implicits._
import com.monovore.decline._
import com.monovore.decline.effect.CommandIOApp
import zio.Runtime.Managed
import zio.interop.catz._
import zio.{Task, ZIO, Managed}
import zio.console.{putStr => p, putStrLn => pl}
import ProjectInfo._

import scala.io.{BufferedSource, Source}

object CliApp
  extends zio.App {

  def run(args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (for {
      pi <- camundala
      p <- run(pi.copy(name = "Standalone Console", sourceUrl = s"${pi.sourceUrl}/cli"), args)
    } yield p)
      .catchAll(e => pl(s"ERROR: $e").as(1))

  def run(projectInfo: ProjectInfo, args: List[String]): ZIO[zio.ZEnv, Nothing, Int] =
    (intro *>
      printProject(projectInfo) *>
      (for {
        input <- zio.console.getStrLn
        _ <- CommandIOApp.run(command, input.split(" ").toList)
      } yield ())
        .tapError(e => zio.console.putStrLn(s"Error: $e"))
        .forever
      ).fold(
      e => 1,
      _ => 0
    )

  private val width = 84
  private val versionFile: zio.Managed[Throwable, BufferedSource] = zio.Managed.make(ZIO.effect(Source.fromFile("./version")))(s => ZIO.succeed(s.close()))

  private val camundala: Task[ProjectInfo] =
    for {
      version <- versionFile.use(vf => ZIO.effect(vf.getLines().next()))
    } yield ProjectInfo("camundala", "pme123", version, "https://github.com/pme123/camundala", "MIT")

  private val intro =
    for {
      - <- p(Console.MAGENTA)
      _ <- pl("*" * width)
      _ <- p(
        """|     _____
           |  __|___  |__  ____    ____    __  __   _  ____   _  _____   ____    ____    ____
           | |   ___|    ||    \  |    \  /  ||  | | ||    \ | ||     \ |    \  |    |  |    \
           | |   |__     ||     \ |     \/   ||  |_| ||     \| ||      \|     \ |    |_ |     \
           | |______|  __||__|\__\|__/\__/|__||______||__/\____||______/|__|\__\|______||__|\__\
           |    |_____|""".stripMargin)
      pi <- camundala
      _ <- line(versionLabel, pi.version, leftAligned = false)
      _ <- line(licenseLabel, pi.license, leftAligned = false)
      _ <- line("", pi.sourceUrl, leftAligned = false)
      _ <- pl("")
      _ <- pl("*" * width)
      _ <- pl(" For Help type '--help'")
      - <- p(Console.RESET)
    } yield ()

  private def printProject(projectInfo: ProjectInfo) = {
    for {
      - <- p(Console.BLUE)
      _ <- line(nameLabel, projectInfo.name)
      _ <- line(orgLabel, projectInfo.org)
      _ <- line(versionLabel, projectInfo.version)
      _ <- line(licenseLabel, projectInfo.license)
      _ <- line(sourceUrlLabel, projectInfo.sourceUrl)
      _ <- pl("")
      _ <- pl("-" * width)
      - <- p(Console.RESET)
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

case class ProjectInfo(name: String, org: String, version: String, sourceUrl: String, license: String = "MIT")

object ProjectInfo {
  val nameLabel = "Project: "
  val orgLabel = "Organization: "
  val versionLabel = "Version: "
  val sourceUrlLabel = "Source Code: "
  val licenseLabel = "License: "

}