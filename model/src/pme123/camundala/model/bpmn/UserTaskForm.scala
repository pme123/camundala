package pme123.camundala.model.bpmn

sealed trait UserTaskForm {

  def staticFiles: Set[StaticFile] = Set.empty

}

object UserTaskForm {

  case class EmbeddedDeploymentForm(form: StaticFile)
    extends UserTaskForm {
    override def staticFiles: Set[StaticFile] = Set(form)

  }
}
