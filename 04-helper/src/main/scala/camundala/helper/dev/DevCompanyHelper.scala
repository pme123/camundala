package camundala.helper.dev

import camundala.api.{defaultProjectConfigPath, projectsPath}
import camundala.helper.dev.company.CompanyGenerator
import camundala.helper.dev.update.createIfNotExists
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
        args.toList match
          case Seq(projectName) =>
            createProject(projectName)
          case other =>
            println(s"Invalid arguments for command $command: $other")
            println("Usage: project <projectName>")
      // dev-company/company-camundala/helper.scala

  enum Command:
    case init, project

  protected def initCompany: Unit =
    println(s"Init Company $companyName")
    given config: DevConfig = DevConfig.configForCompany(s"$companyName-camundala")
    CompanyGenerator().generate

  protected def createProject(projectName: String): Unit =
    println(s"Create Project: ${projectName.replace(s"-$companyName", "")} - Company: $companyName")
    val name = s"$companyName-${projectName.replace(s"$companyName-", "")}"
    val configPath = projectsPath / name / defaultProjectConfigPath
    createIfNotExists(configPath, apiProjectConfig(name))
    given config: DevConfig = DevConfig.init(configPath)
    CompanyGenerator().createProject

  private lazy val companyName = os.pwd.last.replace("dev-", "").toLowerCase

  private def apiProjectConfig(projectName: String) =
    s"""// Project configuration
       |
       |projectName: $projectName
       |projectVersion: 0.1.0-SNAPSHOT
       |subProjects: [
       |  // subProject1
       |  // subProject2
       |]
       |dependencies: [
       |  // mastercompany-services
       |  // mycompany-commons
       |]
       |""".stripMargin

end DevCompanyHelper
