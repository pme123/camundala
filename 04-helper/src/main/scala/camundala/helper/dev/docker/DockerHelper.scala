package camundala.helper.dev.docker

import camundala.helper.util.{DockerConfig, Helpers}

case class DockerHelper(dockerConfig: DockerConfig) extends Helpers:

  val dockerDir = dockerConfig.dockerDir

  def dockerUp(): Unit =
    dockerConfig.prepareDocker()

    println(s"Docker Images are starting - see the Docker Logs.")
    runDocker("up")
    dockerConfig.checkPorts.foreach:
      case label -> port =>
        check(label, port)

    println(s"Docker Images started successfully.")
    dockerConfig.printResults.foreach:
      println

  end dockerUp

  def dockerStop(): Unit =
    print("Stop")
    println(s"Docker Images are stopping - see the Docker Logs.")
    runDocker("stop")
  end dockerStop

  def dockerDown(): Unit =
    print("Down")
    println(
      s"Docker Images are stopping and will be removed - see the Docker Logs."
    )
    runDocker("down")
  end dockerDown

  private def print(
      command: String
  ): Unit =
    println(s"""$command Docker-Compose:
               |- Docker Directory:      $dockerDir""".stripMargin)
  end print

  private def runDocker(command: String) =
    os.proc(
      "docker-compose",
      dockerConfig.dockerComposePaths
        .flatMap: p =>
          Seq("-f", (dockerDir / p).toString),
      "--project-directory",
      s"$dockerDir",
      "-p",
      s"helper-docker",
      command,
      command match
        case "up" => "-d"
        case "stop" => Seq("--timeout", "15") // the only valid param
        case "down" =>
          Seq(
            "--volumes",
            "--remove-orphans"
          ) // Remove containers for services not defined in the Compose file
    ).callOnConsole()
end DockerHelper
