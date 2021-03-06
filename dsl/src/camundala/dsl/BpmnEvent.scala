package camundala.dsl

sealed trait BpmnEvent extends IdentifiableNode

case class StartEvent(id: Identifier,
                      maybeForm: Option[BpmnForm] = None,
                      properties: Properties = Properties.none,
                      outFlows: Seq[SequenceFlow] = Seq.empty
                     )
  extends BpmnEvent


case class EndEvent(id: Identifier,
                    properties: Properties = Properties.none,
                    inputParams: Parameters = Parameters.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends BpmnEvent



