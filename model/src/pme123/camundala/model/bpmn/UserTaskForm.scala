package pme123.camundala.model.bpmn

sealed trait UserTaskForm

object UserTaskForm {
  case class EmbeddedDeploymentForm(form: StaticFile) extends UserTaskForm
}
