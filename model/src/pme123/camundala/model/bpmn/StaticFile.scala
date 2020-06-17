package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

/**
  * The pathWithName is where the file is located in the Resources.
  *
  * @param fileName     The name of the File / Path that is put in the Deployment for Camunda
  * @param resourcePath The rest of the path of where it is found in the resources
  * @param includes     You can add String snippets to the start of the file - e.g. for Scripts
  */
case class StaticFile(fileName: FilePath,
                      resourcePath: PathElem = StaticFile.DefaultDirectory,
                      includes: Seq[String] = Seq.empty) {

  def pathWithName = s"$resourcePath/$fileName"

  def fileNameWithoutExtension: String =
    fileName.value
      .split('.')
      .dropRight(1)
      .mkString(".")

  def generate() =
    s"""StaticFile("${fileName.value}", "${resourcePath.value}")"""

  def generateDsl() = s""""${fileName.value}""""

}

object StaticFile {
  private val DefaultDirectory: PathElem = "bpmn"

  def apply(fileName: FilePath): StaticFile = new StaticFile(fileName)
}
