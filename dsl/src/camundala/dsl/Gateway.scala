package camundala.dsl

sealed trait Gateway extends IdentifiableNode

case class ExclusiveGateway(id: Identifier,
                            properties: Properties = Properties.none,
                            inFlows: Seq[SequenceFlow] = Seq.empty,
                            outFlows: Seq[SequenceFlow] = Seq.empty
                           ) extends Gateway

case class ParallelGateway(id: Identifier,
                           properties: Properties = Properties.none,
                           inFlows: Seq[SequenceFlow] = Seq.empty,
                           outFlows: Seq[SequenceFlow] = Seq.empty
                          ) extends Gateway


