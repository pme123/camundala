package camundala.helper.setup

val doNotAdjust = "DO NOT ADJUST"

case class SetupElement(
                        label: String, processName: String, bpmnName: String, version: Option[Int]
                      )(using setupConfig: SetupConfig):
  
  println(s"Create $label: $bpmnName in ${setupConfig.projectName} / process: $processName v$version")

def createOrUpdate(file: os.Path, contentNew: String): Unit =
  val contentExisting =
    if os.exists(file)
    then os.read(file)
    else doNotAdjust
  val contentUpdated =
    if contentExisting.contains(doNotAdjust) 
    then contentNew
    else
      println(s"${Console.RED}File $file was not updated! - if you want so add $doNotAdjust at the top of this file.${Console.RESET}")
      contentExisting
  os.write.over(file, contentUpdated)
  
end createOrUpdate

extension (version: Option[Int])
  def versionPath: Seq[String] =
    version.map(v => s"v$v").toSeq
  def versionPackage: String =
    version.map(v => s".v$v").getOrElse("")
