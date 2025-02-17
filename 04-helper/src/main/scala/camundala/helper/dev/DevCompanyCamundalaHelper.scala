package camundala.helper.dev

import camundala.helper.dev.company.docs.DocCreator
import camundala.helper.dev.publish.PublishHelper
import camundala.helper.dev.publish.PublishHelper.*
import camundala.helper.util.{DevConfig, PublishConfig}

import scala.util.{Failure, Success, Try}

// dev-company/company-camundala/helper.scala
trait DevCompanyCamundalaHelper extends DocCreator:
  def devConfig: DevConfig

  def runForCompany(command: String, arguments: String*): Unit =
    val args = arguments.toSeq
    println(s"Running command: $command with args: $args")
    Try(Command.valueOf(command)) match
      case Success(cmd) =>
        runCommand(cmd, args)
      case Failure(_)   =>
        println(s"Command not found: $command")
        println("Available commands: " + Command.values.mkString(", "))
    end match
  end runForCompany

  protected def publishConfig: Option[PublishConfig] = devConfig.publishConfig

  private def runCommand(command: Command, args: Seq[String]): Unit =
    command match
      case Command.publish if args.size == 1 =>
        publish(args.head)
      case Command.publish                   =>
        println("Usage: publish <version>")
      case Command.prepareDocs               =>
        prepareDocs()
      case Command.publishDocs               =>
        publishDocs()

  private enum Command:
    case publish, prepareDocs, publishDocs

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

end DevCompanyCamundalaHelper
