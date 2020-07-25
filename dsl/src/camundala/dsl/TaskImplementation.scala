package camundala.dsl

import eu.timepit.refined.auto._

sealed trait TaskImplementation

case class Expression(expresssion: String)
  extends TaskImplementation

case class DelegateExpression(expresssion: String)
  extends TaskImplementation

case class JavaClass(className: String)
  extends TaskImplementation

case class ExternalTask(topic: String)
  extends TaskImplementation

/**
  * Only for BusinessRuleTask
  */
case class DmnImpl(decisionRef: Identifier,
                   resultVariable: Identifier = "ruleResult",
                   decisionRefBinding: String = "latest", // only supported
                   mapDecisionResult: String = "singleEntry", // only supported
                  )
  extends TaskImplementation
