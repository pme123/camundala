package camundala.dsl

case class SequenceFlow(id: Identifier,
                        maybeExpression: Option[ConditionExpression] = None,
                        properties: Properties = Properties.none)
