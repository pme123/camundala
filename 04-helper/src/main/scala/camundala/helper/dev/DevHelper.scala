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
        update
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

  def createProcess(processName: String, version: Option[Int])(using
      config: DevConfig
  ): Unit =
    SetupGenerator().createProcess(SetupElement(
      "Process",
      processName.asProcessName,
      processName.asElemName,
      version
    ))
  end createProcess

  def createCustomTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "CustomTask",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  def createServiceTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createProcessElement(SetupElement(
      "ServiceTask",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  def createUserTask(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createUserTask(
      SetupElement("UserTask", processName.asProcessName, bpmnName.asElemName, version)
    )

  def createDecision(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createDecision(
      SetupElement("Decision", processName.asProcessName, bpmnName.asElemName, version)
    )

  def createSignalEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Signal",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  def createMessageEvent(processName: String, bpmnName: String, version: Option[Int])(
      using config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Message",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  def createTimerEvent(processName: String, bpmnName: String, version: Option[Int])(using
      config: DevConfig
  ): Unit =
    SetupGenerator().createEvent(SetupElement(
      "Timer",
      processName.asProcessName,
      bpmnName.asElemName,
      version
    ))

  extension (name: String)
    def asProcessName: String =
      name.head.toLower + name.tail
    def asElemName: String =
      name.head.toUpper + name.tail
end DevHelper
