package camundala.helper.util

case class DockerConfig(
    prepareDocker: () => Unit = () => (),
    // relative from the project root
    dockerDir: os.RelPath = os.rel / os.up / os.up / "docker",
    // relative from the dockerDir
    dockerComposePaths: Seq[os.RelPath] = Seq(os.rel / "docker-compose.yml"),
    checkPorts: Seq[(String, Int)] = Seq("Camunda" -> 8080),
    printResults: Seq[String] = Seq("Check Camunda on http://localhost:8080")
)
