package camundala.dsl

case class CallActivity(id: Identifier,
                        /*  calledElement: CalledBpmn = CalledBpmn.notImplemented,
                          extInOuts: ExtCallActivityInOuts = ExtCallActivityInOuts.none,
                        */ properties: Properties = Properties.none,
                        inputParams: Parameters = Parameters.none,
                        outputParams: Parameters = Parameters.none,
                        inFlows: Seq[SequenceFlow] = Seq.empty,
                        outFlows: Seq[SequenceFlow] = Seq.empty
                       )
