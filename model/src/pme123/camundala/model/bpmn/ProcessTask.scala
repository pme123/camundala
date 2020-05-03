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

case class UserTask(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask

