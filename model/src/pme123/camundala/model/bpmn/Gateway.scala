package pme123.camundala.model.bpmn

trait Gateway
  extends BpmnNode
    with HasExtProperties {
}

case class ExclusiveGateway(id: BpmnNodeId,
                            extProperties: ExtProperties = ExtProperties.none,
                            inFlows: Seq[SequenceFlow] = Seq.empty,
                            outFlows: Seq[SequenceFlow] = Seq.empty
                           )
  extends Gateway
    with HasInFlows
    with HasOutFlows {

}

case class ParallelGateway(id: BpmnNodeId,
                           extProperties: ExtProperties = ExtProperties.none,
                           inFlows: Seq[SequenceFlow] = Seq.empty,
                           outFlows: Seq[SequenceFlow] = Seq.empty
                          )
  extends Gateway
    with HasInFlows
    with HasOutFlows {

}
