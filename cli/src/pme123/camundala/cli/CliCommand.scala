package pme123.camundala.cli

import com.monovore.decline.Opts
import com.monovore.decline.refined._
import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.{BpmnId, DeployId}

object CliCommand {

  private val deployId: Opts[DeployId] = Opts.argument[DeployId](metavar = "deployId")
    .withDefault("default")

  lazy val validateBpmnOpts: Opts[ValidateBpmn] =
    Opts.subcommand("validate", "Validate a BPMN if it can be merged.") {
      Opts.argument[BpmnId](metavar = "bpmnId")
        .map(ValidateBpmn)
    }

  lazy val generateBpmnsOpts: Opts[GenerateBpmns] =
    Opts.subcommand("generate", "Generates BPMNs that are configured in the Deploy.") {
      deployId
        .map(GenerateBpmns)
    }

  lazy val createUsersAndGroupsOpts: Opts[CreateUsersAndGroups] =
    Opts.subcommand("createUsers", "Creates all Groups and Users that are used in the Deploy.") {
      deployId
        .map(CreateUsersAndGroups)
    }

  lazy val deployCreateOpts: Opts[ComDeploy.Create] =
    Opts.subcommand("create", "Deploy BPMNs to Camunda") {
      deployId
        .map(ComDeploy.Create)
    }

  lazy val deployDeleteOpts: Opts[ComDeploy.Delete] =
    Opts.subcommand("delete", "Undeploy BPMNs from Camunda") {
      deployId
        .map(ComDeploy.Delete)
    }

  lazy val deployListOpts: Opts[ComDeploy.List] =
    Opts.subcommand("list", "Get a list of Camunda Deployments") {
      deployId
        .map(ComDeploy.List)
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
  val appUpdateOpts: Opts[App.Update] = Opts.subcommand("update", "Update Application (Registers)") {
    Opts.unit.map(_ => App.Update())
  }

  case class ValidateBpmn(bpmnId: BpmnId)

  case class GenerateBpmns(deployId: DeployId)

  case class CreateUsersAndGroups(deployId: DeployId)

  // prefixed with Com(mand) as there is already Deploy as a Domain Entity.
  sealed trait ComDeploy

  object ComDeploy {

    case class List(deployId: DeployId = DeployId)

    case class Create(deployId: DeployId = DeployId)

    case class Delete(deployId: DeployId = DeployId)

  }

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

    case class Update()

  }


}
