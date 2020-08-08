package camundala.tools

import camundala.tools.ToolsModule.Tools
import zio.logging.Logging
import zio.logging.slf4j.Slf4jLogger
import zio.{TaskLayer, ULayer}

object ToolsLayers {

  def logLayer(loggerName: String, prefix: String = "camundala.tools"): ULayer[Logging] =
    Slf4jLogger.make((_, message) =>
      message,
      Some(s"$prefix.$loggerName")
    )

  def toolsLayer: TaskLayer[Tools] = logLayer("Tools") >>> ToolsModule.live



}
