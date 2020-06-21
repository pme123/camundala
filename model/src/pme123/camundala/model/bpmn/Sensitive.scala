package pme123.camundala.model.bpmn

case class Sensitive(secret: Password) {
  def value: String = secret.value
  override def toString: String = "*" * 10
}
