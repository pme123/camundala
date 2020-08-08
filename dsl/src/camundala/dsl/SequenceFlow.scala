package camundala.dsl

case class SequenceFlow(id: Identifier,
                        maybeCondition: Option[Condition] = None,
                        properties: Properties = Properties.none
                       ) extends ProcessNode
