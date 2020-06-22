package pme123.camundala.model.bpmn

trait HasInFlows {
  def inFlows: Seq[SequenceFlow]

  def generateInFlowDsl: String =
    inFlows.map(f => s"\n   .inFlow(${f.idVal})").mkString
}

trait HasOutFlows {
  def outFlows: Seq[SequenceFlow]

  def generateOutFlowDsl: String =
    outFlows.map(f => s"\n   .outFlow(${f.idVal})").mkString
}

trait WithInFlow[T] {
  def inFlow(hasFlow: T, flow: SequenceFlow): T
}

object WithInFlow {

  def apply[A](implicit withInFlow: WithInFlow[A]): WithInFlow[A] = withInFlow

  //needed only if we want to support notation: show(...)
  def inFlow[A: WithInFlow](hasFlow: A, flow: SequenceFlow): A =
    WithInFlow[A].inFlow(hasFlow, flow)

  //type class instances
  def instance[A](func: (A, SequenceFlow) => A): WithInFlow[A] =
    (hasFlow: A, flow: SequenceFlow) => func(hasFlow, flow)

  implicit val endEvent: WithInFlow[EndEvent] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val userTask: WithInFlow[UserTask] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val serviceTask: WithInFlow[ServiceTask] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val sendTask: WithInFlow[SendTask] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val businessRuleTask: WithInFlow[BusinessRuleTask] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val exclusiveGateway: WithInFlow[ExclusiveGateway] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))
  implicit val parallelGateway: WithInFlow[ParallelGateway] =
    instance((node, flow) => node.copy(inFlows = node.inFlows :+ flow))

}

trait WithOutFlow[T] {
  def outFlow(hasFlow: T, flow: SequenceFlow): T
}

object WithOutFlow {

  def apply[A](implicit withOutFlow: WithOutFlow[A]): WithOutFlow[A] = withOutFlow

  //needed only if we want to support notation: show(...)
  def outFlow[A: WithOutFlow](hasFlow: A, flow: SequenceFlow): A =
    WithOutFlow[A].outFlow(hasFlow, flow)

  //type class instances
  def instance[A](func: (A, SequenceFlow) => A): WithOutFlow[A] =
    (hasFlow: A, flow: SequenceFlow) => func(hasFlow, flow)

  implicit val startEvent: WithOutFlow[StartEvent] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val userTask: WithOutFlow[UserTask] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val serviceTask: WithOutFlow[ServiceTask] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val sendTask: WithOutFlow[SendTask] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val businessRuleTask: WithOutFlow[BusinessRuleTask] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val exclusiveGateway: WithOutFlow[ExclusiveGateway] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))
  implicit val parallelGateway: WithOutFlow[ParallelGateway] =
    instance((node, flow) => node.copy(outFlows = node.outFlows :+ flow))

}
