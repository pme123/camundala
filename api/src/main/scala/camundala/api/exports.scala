package camundala.api

import os.CommandResult

val catalogFileName = "catalog.md"

def serviceNameDescr(serviceName: String) = s"As this uses the generic Service you need to name the Service to '$serviceName'."

val doMockDescr = "Flag that indicates that the subprocesses should be mocked."
val mockedDescr = "Flag that indicates that this process sould be mocked."
val customMockDescr = "You can override the defaultMock that is returned by the Process. This may be specific to the Process!"
val testModeDescr = "This flag indicades that this is a test - in the process it can behave accordingly."
val handledErrorsDescr = "A comma separated list of HTTP-Status-Codes, that are modelled in the BPMN as Business-Exceptions - see Outputs. z.B: `404,500`"

extension (proc: os.proc) {
  def callOnConsole(path: os.Path = os.pwd): CommandResult =
    proc.call(cwd = path, stdout = os.Inherit)
}