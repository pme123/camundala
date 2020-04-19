package pme123.camundala.model

trait BpmnElem {
  def id: String
}

trait ServiceTask extends BpmnElem {
  def extensions: Extensions
}

case class GenericServiceTask(id: String, extensions: Extensions = Extensions.none)
  extends ServiceTask

object Extensions {
  val none: Extensions = Extensions()
}

case class Extensions(properties: Map[String, String] = Map.empty)