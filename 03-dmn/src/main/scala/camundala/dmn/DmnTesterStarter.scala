package camundala.dmn

import sttp.client3.*

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

trait DmnTesterStarter extends DmnTesterHelpers, App:

  def startDmnTester(): Unit =
    println("Check logs in Docker Console!")
    println(s"Open the browser: http://localhost:${starterConfig.exposedPort}")
    if (checkIsRunning())
      println(s"Port ${starterConfig.exposedPort} is running")
    else
      runDocker()
      waitForServer
  end startDmnTester

  @tailrec
  protected final def waitForServer: Boolean =
    if (checkIsRunning()) true
    else
      println("Waiting for server")
      Thread.sleep(1000)
      waitForServer

  private def checkIsRunning(): Boolean =
    Try(
      client
        .send(
          basicRequest
            .get(uri"$infoUrl")
            .response(asString)
        )
        .body match
        case Left(_) =>
          false
        case Right(result) if !result.contains(getClass.getName) =>
          println(
            s"Docker is Running - BUT for another project: $result. This project: ${getClass.getName}"
          )
          stopDocker()
          runDocker()
          waitForServer
          true
        case Right(result) =>
          println(s"Docker is Running for project: $result.")
          true
    ) match
      case Success(value) => value
      case Failure(_) => false

  protected def runDocker(): Unit =
    println(s"Start Docker for ${starterConfig.containerName}!")
    os.proc(
      "docker",
      "run",
      "--name",
      starterConfig.containerName,
      "--rm",
      "-d",
      "-e",
      s"STARTING_APP=${getClass.getName}",
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
    ).callOnConsole()
  end runDocker

  private def stopDocker(): Unit =
    println(s"Stopping Docker ${starterConfig.containerName}!")
    os.proc(
      "docker",
      "stop",
      starterConfig.containerName
    ).callOnConsole()
