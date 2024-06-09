package camundala.dmn

import os.CommandResult
import sttp.client3.*

trait DmnTesterHelpers:
  protected def starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig()
  protected def projectBasePath: os.Path = os.pwd
  private lazy val exposedPort: Int = starterConfig.exposedPort
  protected lazy val client: SimpleHttpClient = SimpleHttpClient()
  protected lazy val apiUrl = s"http://localhost:$exposedPort/api"
  protected lazy val infoUrl = s"http://localhost:$exposedPort/info"

  protected case class DmnTesterStarterConfig(
      // the name of the container that will be started
      containerName: String = "camunda-dmn-tester",
      // path to where the configs should be created in
      dmnConfigPaths: Seq[os.Path] = Seq(
        projectBasePath / "src" / "main" / "resources" / "dmnConfigs"
      ),
      // paths where the DMNs are (could be different places)
      dmnPaths: Seq[os.Path] = Seq(
        projectBasePath / "src" / "main" / "resources"
      ),
      // the port the DMN Tester is started - e.g. http://localhost:8883
      exposedPort: Int = 8883,
      // the image version of the DMN Tester
      imageVersion: String = "latest"
  )

  extension (proc: os.proc)
    def callOnConsole(path: os.Path = os.pwd): CommandResult =
      proc.call(cwd = path, stdout = os.Inherit)

end DmnTesterHelpers
