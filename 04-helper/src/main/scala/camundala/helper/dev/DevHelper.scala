package camundala.helper.dev

import camundala.api.ApiConfig
import camundala.helper.dev.deploy.DeployHelper
import camundala.helper.dev.docker.DockerHelper
import camundala.helper.dev.publish.PublishHelper
import camundala.helper.util.*
import camundala.helper.dev.update.*

import scala.util.{Failure, Success, Try}

trait DevHelper:
  def apiConfig: ApiConfig
  def devConfig: DevConfig
  def publishConfig: Option[PublishConfig]
  def deployConfig: Option[DeployConfig]
  def dockerConfig: DockerConfig
  given DevConfig = devConfig
  given ApiConfig = apiConfig
  given Option[PublishConfig] = publishConfig

  def run(command: String, arguments: String*): Unit =
    val args = arguments.toSeq
    println(s"Running command: $command with args: $args")
    Try(Command.valueOf(command)) match
      case Success(cmd) =>
        runCommand(cmd, args)
      case Failure(_) =>
        println(s"Command not found: $command")
        println("Available commands: " + Command.values.mkString(", "))
    end match
  end run

  private def runCommand(command: Command, args: Seq[String]): Unit =
    command match
      case Command.update =>
        update()
      // start code generation
      case Command.process =>
        args match
          case Seq(processName) =>
            createProcess(processName, None)
          case Seq(processName, version) if version.toIntOption.isDefined =>
            createProcess(processName, version.toIntOption)
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println("Usage: process <processName> [version: Int]")
            println("Example: process myProcess 1")
      case Command.customTask =>
        args match
          case Seq(processName, bpmnName) =>
            createCustomTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createCustomTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.serviceTask =>
        args match
          case Seq(processName, bpmnName) =>
            createServiceTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createServiceTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.userTask =>
        args match
          case Seq(processName, bpmnName) =>
            createUserTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createUserTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.decision =>
        args match
          case Seq(processName, bpmnName) =>
            createDecision(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createDecision(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.signalEvent =>
        args match
          case Seq(processName, bpmnName) =>
            createSignalEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createSignalEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.messageEvent =>
        args match
          case Seq(processName, bpmnName) =>
            createMessageEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createMessageEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.timerEvent =>
        args match
          case Seq(processName, bpmnName) =>
            createTimerEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            createTimerEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      // finish code generation
      case Command.publish =>
        args match
          case Seq(version) =>
            PublishHelper().publish(version)
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println(s"Usage: $command <version>")
            println(s"Example: $command 1.23.3")

      case Command.deploy =>
        args match
          case Seq(simulation) =>
            deployConfig
              .map(DeployHelper(_).deploy(Some(simulation)))
              .getOrElse(println("deploy is not supported as there is no deployConfig"))
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println(s"Usage: $command <simulation>")
            println(s"Example: $command OpenAccountSimulation")
      // docker
      case Command.dockerUp =>
        DockerHelper(dockerConfig).dockerUp()
      case Command.dockerStop =>
        DockerHelper(dockerConfig).dockerStop()
      case Command.dockerDown =>
        DockerHelper(dockerConfig).dockerDown()

  private def printBadActivity(command: Command, args: Seq[String]): Unit =
    println(s"Invalid arguments for command $command: $args")
    println(s"Usage: $command <processName> <bpmnName> [version: Int]")
    println(s"Example: $command myProcess My$command 1")
  end printBadActivity

  private enum Command:
    case update, process, customTask, serviceTask, userTask, decision, signalEvent, messageEvent,
      timerEvent, publish, deploy, dockerUp, dockerStop, dockerDown

  def update(): Unit =
    println(s"Update Project: ${devConfig.projectName}")
    println(s" - with Subprojects: ${devConfig.subProjects}")
    SetupGenerator().generate

  def createProcess(processName: String, version: Option[Int]): Unit =
    SetupGenerator().createProcess(SetupElement(
      "Process",
      processName.asProcessName,
      processName.asElemName,
      version
    ))
  end createProcess

  private def createCustomTask(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "CustomTask",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  private def createServiceTask(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "ServiceTask",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  private def createUserTask(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createUserTask(
      SetupElement("UserTask", processName.asProcessName, bpmnName.asElemName, version)
    )

  private def createDecision(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createDecision(
      SetupElement("Decision", processName.asProcessName, bpmnName.asElemName, version)
    )

  private def createSignalEvent(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Signal",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  private def createMessageEvent(processName: String, bpmnName: String, version: Option[Int]): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Message",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  private def createTimerEvent(processName: String, bpmnName: String, version: Option[Int])(using
                                                                                            config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Timer",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  extension (name: String)
    private def asProcessName: String =
      name.head.toLower + name.tail
    private def asElemName: String =
      name.head.toUpper + name.tail
  end extension
end DevHelper
