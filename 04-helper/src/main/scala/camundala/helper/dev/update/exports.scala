package camundala.helper.dev.update

export camundala.helper.util.DevConfig
export camundala.helper.util.ModuleConfig

val doNotAdjust = "DO NOT ADJUST"
private val replaceHelperCommand ="./helper.scala update"
lazy val helperDoNotAdjustText = doNotAdjustText(replaceHelperCommand)
lazy val helperHowToResetText = howToResetText(replaceHelperCommand)
def doNotAdjustText(replaceCommand: String) =
  s"// DO NOT ADJUST. This file is replaced by `$replaceCommand`."
def howToResetText(replaceCommand: String) =
  s"// This file was created with `$replaceCommand` - to reset delete it and run the command."

case class SetupElement(
    label: String,
    processName: String,
    bpmnName: String,
    version: Option[Int]
)(using setupConfig: DevConfig):

  lazy val versionLabel: String = version.versionLabel
  lazy val identifier =
    s"${setupConfig.projectName}-$processName${version.versionLabel}$bpmnIdentifier"
  lazy val identifierShort =
    s"${setupConfig.projectShortName}-$processName${version.versionLabel}$bpmnIdentifier"
  println(
    s"Create $label: $bpmnName in ${setupConfig.projectName} / process: $processName ${version.versionLabel}"
  )
  private lazy val bpmnIdentifier =
    if processName.toLowerCase == bpmnName.toLowerCase then "" else s"-$bpmnName"

end SetupElement

def createOrUpdate(file: os.Path, contentNew: String): Unit =
  val contentExisting =
    if os.exists(file)
    then os.read(file)
    else doNotAdjust
  if contentExisting.contains(doNotAdjust)
  then
    println(s"${Console.BLUE}Updated - $file${Console.RESET}")
    os.write.over(file, contentNew)
  else
    println(s"${Console.RED}NOT Updated - $file${Console.RESET}")
  end if

end createOrUpdate

def createIfNotExists(file: os.Path, contentNew: String): Unit =
  if !os.exists(file) then
    println(s"${Console.BLUE} - NEW: $file${Console.RESET}")
    os.write.over(file, contentNew)
  else
    println(s"EXISTS: $file")
  end if
extension (version: Option[Int])
  def versionPath: String =
    version.map(v => s"v$v").getOrElse("v1")
  def versionLabel: String =
    version.map(v => s"V$v").getOrElse("V1")
  def versionPackage: String =
    version.map(v => s".v$v").getOrElse(".v1")
end extension
