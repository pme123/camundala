package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

sealed trait TaskImplementation

object TaskImplementation {

  case class DelegateExpression(expresssion: String)
    extends TaskImplementation

  case class ExternalTask(topic: String)
    extends TaskImplementation

  case class DmnImpl(decisionRef: StaticFile,
                     resultVariable: Identifier = "ruleResult",
                     decisionRefBinding: String = "deployment", // only supported
                     mapDecisionResult: String = "singleEntry", // only supported
                    )
    extends TaskImplementation

  object DmnImpl{
    def apply(decisionRef: FilePath): DmnImpl = new DmnImpl(StaticFile(decisionRef))
  }
}
