package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropInOutExtensions

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

sealed trait ImplementationTask
  extends ProcessTask {
  def implementation: TaskImplementation
}

case class ServiceTask(id: String,
                       implementation: TaskImplementation,
                       extensions: PropInOutExtensions = PropInOutExtensions.none,
                       inOuts: InputOutputs = InputOutputs.none
                      )
  extends ProcessTask
    with ImplementationTask

case class SendTask(id: String,
                    implementation: TaskImplementation,
                    extensions: PropInOutExtensions = PropInOutExtensions.none,
                    inOuts: InputOutputs = InputOutputs.none
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
                    extensions: PropInOutExtensions = PropInOutExtensions.none,
                    inOuts: InputOutputs = InputOutputs.none
                   )
  extends ProcessTask
    with HasForm {


}


