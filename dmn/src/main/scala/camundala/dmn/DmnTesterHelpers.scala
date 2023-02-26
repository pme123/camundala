package camundala.dmn

import os.CommandResult
import sttp.client3.*

import scala.annotation.tailrec
import scala.util.*

trait DmnTesterHelpers:
  protected def starterConfig: DmnTesterStarterConfig = DmnTesterStarterConfig()
  protected def projectBasePath: os.Path = os.pwd
  protected def exposedPort: Int = starterConfig.exposedPort
  protected lazy val client: SimpleHttpClient = SimpleHttpClient()
  protected lazy val apiUrl = s"http://localhost:$exposedPort/api"
  protected lazy val infoUrl = s"http://localhost:$exposedPort/info"


  protected case class DmnTesterStarterConfig(
      containerName: String = "camunda-dmn-tester",
      dmnConfigPaths: Seq[os.Path] = Seq(
        projectBasePath / "src" / "it" / "resources" / "dmnConfigs"
      ),
      dmnPaths: Seq[os.Path] = Seq(
        projectBasePath / "src" / "main" / "resources"
      ),
      exposedPort: Int = 8883,
      imageVersion: String = "latest"
  )

  extension (proc: os.proc)
    def callOnConsole(path: os.Path = os.pwd): CommandResult =
      proc.call(cwd = path, stdout = os.Inherit)

end DmnTesterHelpers
