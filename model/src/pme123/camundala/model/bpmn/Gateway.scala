package pme123.camundala.model.bpmn

trait Gateway
  extends BpmnNode
    with Extensionable

case class ExclusiveGateway(id: String, extensions: Extensions)
  extends Gateway

case class ParallelGateway(id: String, extensions: Extensions)
  extends Gateway
