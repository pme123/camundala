package camundala.helper

import camundala.helper.setup.SetupConfig

case class HelperConfig(
    setupConfig: SetupConfig
)

object ProjectHelper:
  def config(projectName: String) = HelperConfig(
    setupConfig = SetupConfig(projectName)
  )
end ProjectHelper
