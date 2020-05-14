package pme123.camundala.cli

import com.monovore.decline.Opts
import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.BpmnId
import pme123.camundala.model.deploy.DeployId
import com.monovore.decline.refined._

object CliCommand {

  private val deployId: Opts[DeployId] = Opts.argument[DeployId](metavar = "deployId")
    .withDefault("default")

  lazy val validateBpmnOpts: Opts[ValidateBpmn] =
    Opts.subcommand("validate", "Validate a BPMN if it can be merged") {
      Opts.argument[BpmnId](metavar = "bpmnId")
        .map(ValidateBpmn)
    }

  lazy val deployBpmnOpts: Opts[DeployBpmn] =
    Opts.subcommand("deploy", "Deploy BPMNs to Camunda") {
      deployId
        .map(DeployBpmn)
    }

  lazy val undeployBpmnOpts: Opts[UndeployBpmn] =
    Opts.subcommand("undeploy", "Undeploy BPMNs from Camunda") {
      deployId
        .map(UndeployBpmn)
    }

  lazy val deploymentsOpts: Opts[Deployments] =
    Opts.subcommand("deployments", "Get a list of Camunda Deployments") {
      Opts.unit
        .map(_ => Deployments())
    }

  val dockerUpOpts: Opts[Docker.Up] = Opts.subcommand("up", "Docker Compose Up") {
    deployId.map(Docker.Up)
  }
  val dockerStopOpts: Opts[Docker.Stop] = Opts.subcommand("stop", "Docker Compose Stop") {
    deployId.map(Docker.Stop)
  }
  val dockerDownOpts: Opts[Docker.Down] = Opts.subcommand("down", "Docker Compose Down") {
    deployId.map(Docker.Down)
  }

  val appStartOpts: Opts[App.Start] = Opts.subcommand("start", "Start Application") {
    Opts.unit.map(_ => App.Start())
  }
  val appStopOpts: Opts[App.Stop] = Opts.subcommand("stop", "Stop Application") {
    Opts.unit.map(_ => App.Stop())
  }
  val appRestartOpts: Opts[App.Restart] = Opts.subcommand("restart", "Restart Application") {
    Opts.unit.map(_ => App.Restart())
  }

  case class Deployments()

  case class DeployBpmn(deployId: DeployId = "default")

  case class UndeployBpmn(deployId: DeployId = "default")

  case class ValidateBpmn(bpmnId: BpmnId)

  sealed trait Docker

  object Docker {

    case class Up(deployId: DeployId)

    case class Stop(deployId: DeployId)

    case class Down(deployId: DeployId)

  }

  sealed trait App

  object App {

    case class Start()

    case class Stop()

    case class Restart()

  }


}
