package pme123.camundala.model

trait Gateway
  extends BpmnNode
    with Extensionable

case class ExclusiveGateway(id: String, extensions: Extensions)
  extends Gateway