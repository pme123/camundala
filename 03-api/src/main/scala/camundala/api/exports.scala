package camundala.api

import os.CommandResult

val catalogFileName = "catalog.md"

extension (proc: os.proc) {
  def callOnConsole(path: os.Path = os.pwd): CommandResult =
    proc.call(cwd = path, stdout = os.Inherit)
}

@deprecated("Use outputMock, resp. outputMockDescr")
val doMockDescr = "Flag that indicates that the subprocesses should be mocked."
@deprecated("Use outputMock, resp. outputMockDescr")
val mockedDescr = "Flag that indicates that this process sould be mocked."
@deprecated("Use outputMock, resp. outputMockDescr")
val customMockDescr = "You can override the defaultMock that is returned by the Process. This may be specific to the Process!"
