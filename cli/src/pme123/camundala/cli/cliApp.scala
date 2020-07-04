package pme123.camundala.cli

import cats.effect.ExitCode
import com.monovore.decline._
import com.monovore.decline.effect.CommandIOApp
import pme123.camundala.app.appRunner
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.camunda._
import pme123.camundala.camunda.bpmnGenerator.BpmnGenerator
import pme123.camundala.camunda.bpmnService.BpmnService
import pme123.camundala.camunda.httpDeployClient.HttpDeployClient
import pme123.camundala.camunda.scenarioRunner.{ScenarioResult, ScenarioRunner}
import pme123.camundala.camunda.userManagement.UserManagement
import pme123.camundala.cli.ProjectInfo._
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.{Deploy, DockerConfig}
import pme123.camundala.model.register.deployRegister
import pme123.camundala.model.register.deployRegister.DeployRegister
import pme123.camundala.services.dockerComposer
import pme123.camundala.services.dockerComposer.DockerComposer
import zio._
import zio.clock.Clock
import zio.console.{Console, putStr => p, putStrLn => pl}
import zio.interop.catz._
import zio.logging.Logging

object cliApp {

  type CliApp = Has[Service]

  trait Service {
    def run(projectInfo: ProjectInfo): Task[Nothing]
  }

  def run(projectInfo: ProjectInfo): RIO[CliApp, Nothing] =
    ZIO.accessM(_.get.run(projectInfo))

  type CliAppDeps =
    Clock
      with Console
      with UserManagement
      with BpmnService
      with DeployRegister
      with HttpDeployClient
      with DockerComposer
      with BpmnGenerator
      with ScenarioRunner
      with AppRunner
      with Logging

  lazy val live: URLayer[CliAppDeps, CliApp] = ZLayer.fromServices[
    Clock.Service,
    Console.Service,
    userManagement.Service,
    bpmnService.Service,
    deployRegister.Service,
    httpDeployClient.Service,
    dockerComposer.Service,
    bpmnGenerator.Service,
    scenarioRunner.Service,
    appRunner.Service,
    logging.Logger[String],
    Service] {
    (clock, console, userManagmnt, bpmnService, deployReg, deployClient, dockerService, generator, scenarioRunner, appRunner, log) =>

      import CliCommand._

      lazy val command = Command[Task[ExitCode]]("", "CLI for Camunda")(
        (validateBpmnOpts orElse generateBpmnsOpts orElse runScenariosOpts orElse createUsersAndGroupsOpts
          orElse deployCommand orElse dockerCommand orElse appCommand).map {
          case ValidateBpmn(bpmnId) =>
            validateBpmn(bpmnId)
          case GenerateBpmns(deployId) =>
            generateBpmns(deployId)
          case RunScenarios(deployId) =>
            runScenarios(deployId)
          case CreateUsersAndGroups(deployId) =>
            createUsersAndGroups(deployId)
          case other =>
            other.asInstanceOf[UIO[ExitCode]]
        }
      )

      lazy val deployCommand: Opts[ZIO[Any, Nothing, ExitCode]] =
        Opts.subcommand("deploy", "Deploy your BPMNs.") {
          (deployCreateOpts orElse deployDeleteOpts orElse deployListOpts).map {
            case ComDeploy.Create(deployId) =>
              deployCreate(deployId)
            case ComDeploy.Delete(deployId) =>
              deployDelete(deployId)
            case ComDeploy.List(deployId) =>
              deployList(deployId)
          }
        }

      lazy val dockerCommand: Opts[ZIO[Any, Nothing, ExitCode]] =
        Opts.subcommand("docker", "Work with Docker Compose") {
          (dockerUpOpts orElse dockerStopOpts orElse dockerDownOpts).map {
            case Docker.Up(bpmnId) =>
              dockerUp(bpmnId)
            case Docker.Stop(bpmnId) =>
              dockerStop(bpmnId)
            case Docker.Down(bpmnId) =>
              dockerDown(bpmnId)
          }
        }

      lazy val appCommand: Opts[ZIO[Any, Nothing, ExitCode]] =
        Opts.subcommand("app", "Manage your App") {
          (appStartOpts orElse appStopOpts orElse appRestartOpts orElse appUpdateOpts).map {
            case App.Start() =>
              appStart()
            case App.Stop() =>
              appStop()
            case App.Restart() =>
              appRestart()
            case App.Update() =>
              appUpdate()
          }
        }

      def validateBpmn(bpmnId: BpmnId) = {
        (for {
          _ <- appRunner.update()
          valWarns <- bpmnService.validateBpmn(bpmnId)
          result <- printSuccess(s"Successful validated BPMN '$bpmnId'${scala.Console.YELLOW}\nWarnings:",
            valWarns.value.map(_.msg).mkString(" - ", "\n - ", ""))
        } yield result)
          .catchAll(printError(_, "Validation failed:"))
      }

      def createUsersAndGroups(deployId: DeployId) = {
        runWithDeployId[Unit](deployId, "Create Users and Groups", deploy => {
          val groups = deploy.groups()
          val users = deploy.users()
          userManagmnt.initGroupsAndUsers(groups, users)
        },
          _ => ""
        )
      }

      def generateBpmns(deployId: DeployId) =
        runWithDeployId[Seq[Bpmn]](deployId, "Generate BPMNs", deploy =>
          Task.foreach(deploy.bpmns)(b => generator.generate(b.xml)),
          results =>
            results.foldLeft("")((r, bpmn) => s"$r${scala.Console.RESET}\nGenerated ${bpmn.id} - copy and paste the following output to your bpmnModels.sc file: ${scala.Console.GREEN}\n\n${bpmn.generateDsl()}")
        )

      def runScenarios(deployId: DeployId) =
        runWithDeployId[Seq[ScenarioResult]](deployId, "Run Scenarios", deploy =>
          Task.foreach(deploy.scenarios)(scenario => scenarioRunner.run(scenario)),
          results =>
            results.foldLeft("")((r, scenarioResult) => s"$r${scala.Console.RESET}\n$scenarioResult")
        )

      def deployCreate(deployId: DeployId) =
        runWithDeployId[Seq[DeployResult]](deployId, "Deploy BPMN", deploy =>
          deployClient.deploy(deploy),
          results =>
            results.foldLeft("")((r, dr) => s"$r\n${scala.Console.YELLOW}- Warnings ${dr.name}:\n${dr.validateWarnings.value.mkString(" - ", "\n - ", "")}")
        )

      def deployDelete(deployId: DeployId) =
        runWithDeployId[Seq[Unit]](deployId, "Undeploy BPMN", deploy =>
          Task.foreach(deploy.bpmns)(b => deployClient.undeploy(b.id, deploy.camundaEndpoint)),
          _ => ""
        )

      def deployList(deployId: DeployId) =
        runWithDeployId[Seq[DeployResult]](deployId, "Get Deployments", deploy =>
          deployClient.deployments(deploy.camundaEndpoint),
          results => results.mkString(" - ", "\n - ", ""))

      def appStart() =
        runUnit("Start App", () => appRunner.start().as(""))

      def appStop() =
        runUnit("Stop App", () => appRunner.stop().as(""))

      def appRestart() =
        runUnit("Restart App", () => appRunner.restart().as(""))

      def appUpdate() =
        runUnit("Update App (Register)", () => appRunner.update().as(""))

      def dockerUp(deployId: DeployId) =
        runDocker(deployId, "Docker Up", dockerService.runDockerUp(_).provideLayer(ZLayer.succeed(clock)))

      def dockerStop(deployId: DeployId) =
        runDocker(deployId, "Docker Stop", dockerService.runDockerStop)

      def dockerDown(deployId: DeployId) =
        runDocker(deployId, "Docker Down", dockerService.runDockerDown)

      def runDocker(deployId: DeployId, label: String, run: DockerConfig => Task[String]) =
        runWithDeployId[String](deployId, label, deploy => run(deploy.dockerConfig), str => str)

      def runUnit(label: String, run: () => Task[String]) = {
        (for {
          results <- run()
          result <- printSuccess(s"Successful $label", results)
        } yield result)
          .catchAll(printError(_, s"$label failed:"))
      }

      def runWithDeployId[T](deployId: DeployId, label: String, run: Deploy => Task[T], details: T => String) = {
        (for {
          maybeDeploy <- deployReg.requestDeploy(deployId)
          results <-
            if (maybeDeploy.isEmpty)
              ZIO.fail(CliAppException(s"There is no Deployment with the id '$deployId'"))
            else
              run(maybeDeploy.get)
          result <- printSuccess(s"Successful $label", details(results))
        } yield result)
          .catchAll(printError(_, s"$label failed"))
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

      lazy val cliRunner =
        (for {
          input <- console.getStrLn
          _ <- CommandIOApp.run(command, input.split(" ").toList)
        } yield ())
          .forever

      (projectInfo: ProjectInfo) =>
        (intro *>
          printProject(projectInfo) *>
          appStart() *>
          appUpdate() *> // make sure Registry is initialized
          cliRunner)
          .provideLayer(ZLayer.succeed(console))
  }

  private val width = 84

  private[cli] val info = pme123.camundala.BuildInfo

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
           |    |_____|     """.stripMargin + s"Doing Camunda ${info.camundaVersion} by Scala ${info.scalaVersion}")
      _ <- p(scala.Console.MAGENTA)
      _ <- line(versionLabel, info.version, leftAligned = false)
      _ <- line(licenseLabel, info.license, leftAligned = false)
      _ <- line("", info.url, leftAligned = false)
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
