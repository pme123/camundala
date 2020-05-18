package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Extensions.PropInOutExtensions
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

sealed trait ImplementationTask
  extends ProcessTask {
  def implementation: TaskImplementation
}

case class ServiceTask(id: BpmnNodeId,
                       implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                       extensions: PropInOutExtensions = PropInOutExtensions.none,
                       inOuts: InputOutputs = InputOutputs.none
                      )
  extends ProcessTask
    with ImplementationTask

case class SendTask(id: BpmnNodeId,
                    implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
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

case class UserTask(id: BpmnNodeId,
                    maybeForm: Option[UserTaskForm] = None,
                    extensions: PropInOutExtensions = PropInOutExtensions.none,
                    inOuts: InputOutputs = InputOutputs.none
                   )
  extends ProcessTask
    with HasForm {



}


