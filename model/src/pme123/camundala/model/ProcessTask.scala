package pme123.camundala.model

sealed trait ProcessTask
  extends BpmnNode
    with Extensionable

case class ServiceTask(id: String,
                       extensions: Extensions = Extensions.none
                      )
  extends ProcessTask

case class UserTask(id: String,
                    extensions: Extensions = Extensions.none
                   )
  extends ProcessTask

