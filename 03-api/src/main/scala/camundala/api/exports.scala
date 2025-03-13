package camundala.api

import os.CommandResult
import camundala.bpmn.shortenName
import camundala.domain.*

val catalogFileName          = "catalog.md"
val defaultProjectConfigPath = os.rel / "PROJECT.conf"
lazy val projectsPath        = os.pwd / "projects"

def shortenTag(refIdentShort: String) =
  val tag = shortenName(refIdentShort)
  tag.head.toUpper + tag.tail.map {
    case c: Char if c.isUpper => s" $c"
    case c                    => s"$c"
  }.mkString.replace(".", " ").replace("-", " ").replace("_", " ").replace("  ", " ")
end shortenTag

extension (proc: os.proc)
  def callOnConsole(path: os.Path = os.pwd): CommandResult =
    proc.call(cwd = path, stdout = os.Inherit)
