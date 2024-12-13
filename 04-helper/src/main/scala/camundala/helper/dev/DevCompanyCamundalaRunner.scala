package camundala.helper.dev

import camundala.helper.dev.publish.PublishHelper
import camundala.helper.dev.publish.PublishHelper.*
import camundala.helper.util.{DevConfig, Helpers}

import scala.util.{Failure, Success, Try}

// dev-company/company-camundala/helper.scala
case class DevCompanyCamundalaRunner(
    devConfig: DevConfig
) extends Helpers:

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
      case Command.publish if args.size == 1 =>
        publish(args.head)
      case Command.publish =>
        println("Usage: publish <version>")
      case Command.prepareDocs =>
      //   CompanyDocCreator.prepareDocs()
      case Command.releaseDocs =>
      //  CompanyDocCreator.releaseDocs()
      case other =>
        println(s"Command not found: $other")
        println("Available commands: publish, prepareDocs, releaseDocs")

  enum Command:
    case publish, prepareDocs, releaseDocs

  private def publish(newVersion: String): Unit =
    println(s"Publishing ${devConfig.projectName}: $newVersion")
    verifyVersion(newVersion)
    verifySnapshots()
    verifyChangelog(newVersion)
    replaceVersion(newVersion, projectFile)
    println("Versions replaced")
    val isSnapshot = newVersion.contains("-")

    os.proc("sbt", "-J-Xmx3G", "publish").callOnConsole()

    if !isSnapshot then
      git(newVersion, newVers => replaceVersion(newVers, projectFile))
    end if
  end publish

end DevCompanyCamundalaRunner
