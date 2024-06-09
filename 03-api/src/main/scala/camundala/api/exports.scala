package camundala.api

import os.CommandResult

val catalogFileName = "catalog.md"
val defaultProjectPath = os.rel / "PROJECT.conf"
extension (proc: os.proc)
  def callOnConsole(path: os.Path = os.pwd): CommandResult =
    proc.call(cwd = path, stdout = os.Inherit)
