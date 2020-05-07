package pme123.camundala.cli

import cats.effect.ExitCode
import com.monovore.decline._
import com.monovore.decline.effect.CommandIOApp
import pme123.camundala.app.appRunner
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.deploymentService.DeploymentService
import pme123.camundala.camunda.xml.ValidateWarnings
import pme123.camundala.camunda.{bpmnService, deploymentService}
import pme123.camundala.cli.ProjectInfo._
import pme123.camundala.model.bpmn.CamundalaException
import pme123.camundala.model.deploy.deployRegister
import pme123.camundala.model.deploy.deployRegister.DeployRegister
import zio._
import zio.console.{Console, putStr => p, putStrLn => pl}
import zio.interop.catz._
import pme123.camundala.app.appRunner.AppRunner

import scala.io.{BufferedSource, Source}

object cliApp {

  type CliApp = Has[Service]

  trait Service {
    def run(projectInfo: ProjectInfo): ZIO[Console, Throwable, Nothing]
  }

  def run(projectInfo: ProjectInfo): ZIO[CliApp with Console, Throwable, Nothing] =
    ZIO.accessM(_.get.run(projectInfo))

  type CliAppDeps = Console with BpmnService with DeployRegister with DeploymentService with AppRunner

  lazy val live: URLayer[CliAppDeps, CliApp] =
    ZLayer.fromServices[Console.Service, bpmnService.Service, deployRegister.Service, deploymentService.Service, appRunner.Service, Service] {
      (console, bpmnService, deployReg, deployService, appRunner) =>

        lazy val validateBpmnOpts: Opts[ValidateBpmn] =
          Opts.subcommand("validate", "Validate a BPMN if it can be merged") {
            Opts.argument[String](metavar = "bpmnId")
              .map(ValidateBpmn)
          }

        lazy val deployBpmnOpts: Opts[DeployBpmn] =
          Opts.subcommand("deploy", "Deploy BPMNs to Camunda") {
            Opts.argument[String](metavar = "deployId")
              .withDefault("default")
              .map(DeployBpmn)
          }

        lazy val deploymentsOpts: Opts[Deployments] =
          Opts.subcommand("deployments", "Get a list of Camunda Deployments") {
            Opts.unit
              .map(_ => Deployments())
          }

        def command(runningApp: Fiber.Runtime[Throwable, Unit]) = Command[Task[ExitCode]]("", "CLI for Camunda")(
          (validateBpmnOpts orElse deployBpmnOpts orElse deploymentsOpts).map {
            case ValidateBpmn(bpmnId) =>
              validateBpmn(bpmnId)
            case DeployBpmn(deployId) =>
              deployBpmn(deployId, runningApp)
            case Deployments() =>
              (for {
                results <- deployService.deployments()
                result <- printSuccess("Successful got Deployments", results.mkString("\n"))
              } yield result)
                .catchAll(printError(_, "Get Deployments failed:"))
          }
        )

        def validateBpmn(bpmnId: String) = {
          (for {
            valWarns <- bpmnService.validateBpmn(bpmnId)
            result <- printSuccess(s"Successful validated BPMN '$bpmnId'${scala.Console.YELLOW}\nWarnings:",
              valWarns.value.map(_.msg).mkString(" - ", "\n - ", ""))
          } yield result)
            .catchAll(printError(_, "Validation failed:"))
        }

        def deployBpmn(deployId: String, runningApp: Fiber.Runtime[Throwable, Unit]) = {
          (for {
            _ <- runningApp.interrupt
            _ <- runningApp.getRef()
            maybeDeploy <- deployReg.requestDeploy(deployId)
            results <-
              if (maybeDeploy.isEmpty)
                ZIO.fail(CliAppException(s"There is no Deployment with the id '$deployId''"))
              else
                Task.foreach(maybeDeploy.toSeq
                  .flatMap(_.bpmns))(deployService.deploy)
            result <- printSuccess("Successful deployed",
              results.map(_.copy(validateWarnings = ValidateWarnings.none)).mkString("\n") +
                results.foldLeft("")((r, dr) => s"$r\n${scala.Console.YELLOW}- Warnings ${dr.name}:\n${dr.validateWarnings.value.mkString(" - ", "\n - ", "")}"))
          } yield result)
            .catchAll(printError(_, "Deployment failed:"))
        }

        def printSuccess(msg: String, details: String): UIO[ExitCode] = {
          console.putStrLn(s"${scala.Console.GREEN}$msg") *>
            console.putStrLn(details) *>
            console.putStr(scala.Console.RESET) *>
            ZIO.succeed(ExitCode.Success)

        }

        def printError(e: Throwable, msg: String): UIO[ExitCode] = {
          console.putStrLn(s"${scala.Console.RED}$msg") *>
            ZIO.succeed(e.printStackTrace()) *>
            console.putStr(scala.Console.RESET) *>
            ZIO.succeed(ExitCode.Error)
        }

        def cliRunner(runningApp: Fiber.Runtime[Throwable, Unit]) = intro *>
          (for {
            input <- console.getStrLn
            _ <- CommandIOApp.run(command(runningApp), input.split(" ").toList)
          } yield ())
            .tapError(e => console.putStrLn(s"Error: $e"))
            .forever

        case class Deployments()
        case class DeployBpmn(deployId: String = "default")
        case class ValidateBpmn(bpmnId: String)

        (projectInfo: ProjectInfo) =>
        for{
          f <- appRunner.run().fork
          _ <-  printProject(projectInfo)
          _ <- ZIO.effect(Thread.sleep(10000))
          d <-  cliRunner(f)
        } yield d

    }

  private val width = 84
  private val versionFile: zio.Managed[Throwable, BufferedSource] = zio.Managed.make(ZIO.effect(Source.fromFile("./version")))(s => ZIO.succeed(s.close()))

  private[cli] val camundala: Task[ProjectInfo] =
    for {
      version <- versionFile.use(vf => ZIO.effect(vf.getLines().next()))
    } yield ProjectInfo("camundala", "pme123", version, "https://github.com/pme123/camundala")

  private val intro =
    for {
      _ <- p(scala.Console.MAGENTA)
      _ <- pl("*" * width)
      _ <- p(scala.Console.BLUE)
      _ <- p(
        """|     _____
           |  __|___  |__  ____    ____    __  __   _  ____   _  _____   ____    ____    ____
           | |   ___|    ||    \  |    \  /  ||  | | ||    \ | ||     \ |    \  |    |  |    \
           | |   |__     ||     \ |     \/   ||  |_| ||     \| ||      \|     \ |    |_ |     \
           | |______|  __||__|\__\|__/\__/|__||______||__/\____||______/|__|\__\|______||__|\__\
           |    |_____|    Doing Camunda with Scala""".stripMargin)
      _ <- p(scala.Console.MAGENTA)
      pi <- camundala
      _ <- line(versionLabel, pi.version, leftAligned = false)
      _ <- line(licenseLabel, pi.license, leftAligned = false)
      _ <- line("", pi.sourceUrl, leftAligned = false)
      _ <- pl("")
      _ <- pl("*" * width)
      _ <- pl(" For Help type '--help'")
      _ <- p(scala.Console.RESET)
    } yield ()

  private def printProject(projectInfo: ProjectInfo) = {
    for {
      _ <- p(scala.Console.BLUE)
      _ <- line(nameLabel, projectInfo.name)
      _ <- line(orgLabel, projectInfo.org)
      _ <- line(versionLabel, projectInfo.version)
      _ <- line(licenseLabel, projectInfo.license)
      _ <- line(sourceUrlLabel, projectInfo.sourceUrl)
      _ <- pl("")
      _ <- pl("-" * width)
      _ <- p(scala.Console.RESET)
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

  case class CliAppException(msg: String) extends CamundalaException

}