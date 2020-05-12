package pme123.camundala.cli

import com.monovore.decline.Opts
import zio.ZLayer

object CliCommand {

  private val deployId: Opts[String] = Opts.argument[String](metavar = "deployId")
    .withDefault("default")

  lazy val validateBpmnOpts: Opts[ValidateBpmn] =
    Opts.subcommand("validate", "Validate a BPMN if it can be merged") {
      Opts.argument[String](metavar = "bpmnId")
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

  case class Deployments()

  case class DeployBpmn(deployId: String = "default")

  case class UndeployBpmn(deployId: String = "default")

  case class ValidateBpmn(bpmnId: String)

  sealed trait Docker

  object Docker {

    case class Up(deployId: String)

    case class Stop(deployId: String)

    case class Down(deployId: String)

  }


}
