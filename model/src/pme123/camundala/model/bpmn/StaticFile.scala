package pme123.camundala.model.bpmn

case class StaticFile(fileName: String, resourcePath: String) {

  def pathWithName = s"$resourcePath/$fileName"

}
