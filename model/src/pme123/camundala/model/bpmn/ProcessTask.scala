package pme123.camundala.model.bpmn

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

sealed trait ImplementationTask
  extends ProcessTask {
  def implementation: TaskImplementation
}

case class ServiceTask(id: String,
                       implementation: TaskImplementation,
                       extensions: Extensions = Extensions.none
                      )
  extends ProcessTask
    with ImplementationTask

case class SendTask(id: String,
                    implementation: TaskImplementation,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask
    with ImplementationTask

trait HasForm
  extends ProcessTask {

  def maybeForm: Option[UserTaskForm]

  def staticFiles: Set[StaticFile] = maybeForm.toSet[UserTaskForm].flatMap(_.staticFiles)

}

case class UserTask(id: String,
                    maybeForm: Option[UserTaskForm] = None,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask
    with HasForm {


}


