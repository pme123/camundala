package camundala.helper.dev

import camundala.helper.dev.company.CompanyGenerator
import camundala.helper.util.{DevConfig, RepoConfig}

import scala.util.{Failure, Success, Try}

// dev-company/helperCompany.scala
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
      // dev-company/company-camundala/helper.scala

  enum Command:
    case init, project

  private def initCompany: Unit =
    println(s"Init Company $companyName")
    given config: DevConfig = CompanyGenerator.init(companyName)
    CompanyGenerator().generate


  private def createProject(projectName: String): Unit =
    println(s"Create Project: $projectName - Company: $companyName")
    given config: DevConfig = DevConfig.defaultConfig(s"$companyName-$projectName")
    CompanyGenerator().createProject

  private lazy val companyName = os.pwd.last.replace("dev-", "").toLowerCase

end DevCompanyHelper
