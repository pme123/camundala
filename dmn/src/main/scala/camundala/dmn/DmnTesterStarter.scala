package camundala.dmn

import sttp.client3.*

import scala.util.{Failure, Success, Try}

trait DmnTesterStarter:

  def projectBasePath: os.Path = os.pwd

  case class DmnTesterStarterConfig(
      containerName: String = "camunda-dmn-tester",
      dmnConfigPaths: Seq[os.Path] = Seq(projectBasePath / "dmnConfigs"),
      dmnPaths: Seq[os.Path] = Seq(
        projectBasePath / "src" / "main" / "resources"
      ),
      exposedPort: Int = 8883,
      imageVersion: String = "latest"
  )

  def starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig()

  def run(): Unit =
    println("Check logs in Docker Console!")
    if (checkIsRunning())
      println(s"Port ${starterConfig.exposedPort} is running")
    else
      os.proc(
        "docker",
        "run",
        "--name",
        starterConfig.containerName,
        "-d",
        "--rm",
        "-e",
        s"TESTER_CONFIG_PATHS=${starterConfig.dmnConfigPaths
          .map(_.relativeTo(projectBasePath))
          .mkString(",")}",
        "-v",
        starterConfig.dmnPaths.map(p =>
          s"$p:/opt/docker/${p.relativeTo(projectBasePath)}"
        ),
        "-v",
        starterConfig.dmnConfigPaths.map(p =>
          s"$p:/opt/docker/${p.relativeTo(projectBasePath)}"
        ),
        "-p",
        s"${starterConfig.exposedPort}:8883",
        s"pame/camunda-dmn-tester:${starterConfig.imageVersion}"
      ).call()
  end run

  protected lazy val client: SimpleHttpClient = SimpleHttpClient()
  protected lazy val apiUrl = s"http://localhost:${starterConfig.exposedPort}/api"
  protected def checkIsRunning(): Boolean =
    Try(client
      .send(
        basicRequest
          .contentType("application/json")
          .get(uri"$apiUrl/basePath")
          .response(asString)
      )
      .body match
      case Left(exc) =>
        println(s"Docker is not Running.")
        false
      case Right(_) => true
    ) match
      case Success(value) => value
      case Failure(_) => false

object DmnTesterStarter extends DmnTesterStarter, App:
  run()
end DmnTesterStarter
