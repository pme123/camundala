package camundala.helper.dev

import camundala.helper.util.DevConfig
import camundala.helper.dev.update.*

import scala.util.{Failure, Success, Try}

object DevHelper:

  def run(command: String, arguments: String*)(using DevConfig): Unit =
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

  private def runCommand(command: Command, args: Seq[String])(using DevConfig): Unit =
    command match
      case Command.update =>
        DevHelper.update
      case Command.process =>
        args match
          case Seq(processName) =>
            DevHelper.createProcess(processName, None, None)
          case Seq(processName, version) if version.toIntOption.isDefined =>
            DevHelper.createProcess(processName, version.toIntOption, None)
          case Seq(processName, version, subProject) if version.toIntOption.isDefined =>
            DevHelper.createProcess(processName, version.toIntOption, Some(subProject))
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println("Usage: process <processName> [version: Int] [subProject]")
            println("Example: process myProcess 1 subProject1")
      case Command.customTask =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createCustomTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createCustomTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.serviceTask =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createServiceTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createServiceTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.userTask =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createUserTask(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createUserTask(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.decision =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createDecision(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createDecision(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.signalEvent =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createSignalEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createSignalEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.messageEvent =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createMessageEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createMessageEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
      case Command.timerEvent =>
        args match
          case Seq(processName, bpmnName) =>
            DevHelper.createTimerEvent(processName, bpmnName, None)
          case Seq(processName, bpmnName, version) if version.toIntOption.isDefined =>
            DevHelper.createTimerEvent(processName, bpmnName, version.toIntOption)
          case other =>
            printBadActivity(command, other)
            /*
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
            DeployHelper().deploy(Some(simulation))
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println(s"Usage: $command <simulation>")
            println(s"Example: $command OpenAccountSimulation")
      case Command.dockerUp =>
        args match
          case Seq(imageVersion) =>
            DockerHelper().dockerUp(Some(imageVersion))
          case _ =>
            DockerHelper().dockerUp(None)
      case Command.dockerStop =>
        DockerHelper().dockerStop()
      case Command.dockerDown =>
        DockerHelper().dockerDown()
*/
  private def printBadActivity(command: Command, args: Seq[String]): Unit =
    println(s"Invalid arguments for command $command: $args")
    println(s"Usage: $command <processName> <bpmnName> [version: Int]")
    println(s"Example: $command myProcess My$command 1")
  end printBadActivity

  enum Command:
    case update, process, customTask, serviceTask, userTask, decision, signalEvent, messageEvent,
      timerEvent, publish, deploy, dockerUp, dockerStop, dockerDown

  def update(using config: DevConfig): Unit =
    println(s"Update Project: ${config.projectName}")
    println(s" - with Subprojects: ${config.subProjects}")
    SetupGenerator().generate

  def createProcess(processName: String, version: Option[Int], subProject: Option[String])(using
      config: DevConfig
  ): Unit =
    val name =
      subProject.map(_ => processName).getOrElse(processName.head.toUpper + processName.tail)
    val processOrSubProject = subProject.getOrElse(processName)

    SetupGenerator().createProcess(SetupElement(
      "Process",
      processOrSubProject,
      name,
      version
    ))
  end createProcess

  def createCustomTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "CustomTask",
      processName,
      bpmnName,
      version
    ))

  def createServiceTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "ServiceTask",
      processName,
      bpmnName,
      version
    ))

  def createUserTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createUserTask(
      SetupElement("UserTask", processName, bpmnName, version)
    )

  def createDecision(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createDecision(
      SetupElement("Decision", processName, bpmnName, version)
    )

  def createSignalEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Signal",
      processName,
      bpmnName,
      version
    ))

  def createMessageEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Message",
      processName,
      bpmnName,
      version
    ))

  def createTimerEvent(processName: String, bpmnName: String, version: Option[Int])(using
      config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Timer",
      processName,
      bpmnName,
      version
    ))
end DevHelper
