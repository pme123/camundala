package camundala.dsl

case class ServiceTask(id: Identifier,
                       implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                       properties: Properties = Properties.none,
                       inputParams: Parameters = Parameters.none,
                       outputParams: Parameters = Parameters.none,
                          inFlows: Seq[SequenceFlow] = Seq.empty,
                           outFlows: Seq[SequenceFlow] = Seq.empty
                       ) extends ProcessNode
