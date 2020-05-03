package pme123.camundala.model.bpmn

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

case class ServiceTask(id: String,
                       implementation: TaskImplementation,
                       extensions: Extensions = Extensions.none
                      )
  extends ProcessTask

case class SendTask(id: String,
                       implementation: TaskImplementation,
                       extensions: Extensions = Extensions.none
                      )
  extends ProcessTask

case class UserTask(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask

