package camundala.helper.dev

import camundala.helper.dev.company.InitCompanyGenerator
import camundala.helper.util.{DevConfig, RepoConfig}

import scala.util.{Failure, Success, Try}

object DevCompanyHelper:

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
      case Command.`init` =>
        initCompany
      case Command.project =>
        args match
          case Seq(projectName) =>
            createProject(projectName)
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println("Usage: project <projectName>")


  enum Command:
    case init, project

  def initCompany: Unit =
    println(s"Init Company $companyName")
    given config: DevConfig = InitCompanyGenerator.init(companyName)
    InitCompanyGenerator().generate


  def createProject(projectName: String): Unit =
    println(s"Create Project: $projectName - Company: $companyName")
    given config: DevConfig = DevConfig.defaultConfig(s"$companyName-$projectName")
    InitCompanyGenerator().createProject

  private lazy val companyName = os.pwd.last.replace("dev-", "")

end DevCompanyHelper
