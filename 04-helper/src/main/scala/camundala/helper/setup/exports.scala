package camundala.helper.setup

val doNotAdjust = "DO NOT ADJUST"

case class SetupElement(
    label: String,
    processName: String,
    bpmnName: String,
    version: Option[Int]
)(using setupConfig: SetupConfig):

  println(
    s"Create $label: $bpmnName in ${setupConfig.projectName} / process: $processName v$version"
  )
end SetupElement

def createOrUpdate(file: os.Path, contentNew: String): Unit =
  val contentExisting =
    if os.exists(file)
    then os.read(file)
    else doNotAdjust
  val contentUpdated =
    if contentExisting.contains(doNotAdjust)
    then contentNew
    else
      println(s"${Console.RED} - $file${Console.RESET}")
      contentExisting
  os.write.over(file, contentUpdated)

end createOrUpdate

def createIfNotExists(file: os.Path, contentNew: String): Unit =
  if !os.exists(file) then
    println(s"${Console.BLUE} - NEW: $file${Console.RESET}")
    os.write.over(file, contentNew)

extension (version: Option[Int])
  def versionPath: String =
    version.map(v => s"v$v").getOrElse("v1")
  def versionLabel: String =
    version.map(v => s"V$v").getOrElse("V1")
  def versionPackage: String =
    version.map(v => s".v$v").getOrElse(".v1")
end extension
