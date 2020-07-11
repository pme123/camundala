package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.TaskImplementation.{DelegateExpression, DmnImpl}

sealed trait ProcessTask
  extends BpmnNode
    with HasExtProperties
    with HasExtInOutputs {
}

sealed trait ImplementationTask
  extends ProcessTask {

  def implementation: TaskImplementation

  def implStaticFiles: Set[StaticFile] = implementation.staticFiles

}

case class ServiceTask(id: BpmnNodeId,
                       implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                       extProperties: ExtProperties = ExtProperties.none,
                       extInOutputs: ExtInOutputs = ExtInOutputs.none,
                       inFlows: Seq[SequenceFlow] = Seq.empty,
                       outFlows: Seq[SequenceFlow] = Seq.empty
                      )
  extends ImplementationTask
    with HasInFlows
    with HasOutFlows {
}

case class SendTask(id: BpmnNodeId,
                    implementation: TaskImplementation = DelegateExpression("#{YOURAdapter}"),
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty,
                    outFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends ImplementationTask
    with HasInFlows
    with HasOutFlows {
}

case class UserTask(id: BpmnNodeId,
                    candidateGroups: CandidateGroups = CandidateGroups.none,
                    candidateUsers: CandidateUsers = CandidateUsers.none,
                    maybeForm: Option[UserTaskForm] = None,
                    extProperties: ExtProperties = ExtProperties.none,
                    extInOutputs: ExtInOutputs = ExtInOutputs.none,
                    inFlows: Seq[SequenceFlow] = Seq.empty,
                    outFlows: Seq[SequenceFlow] = Seq.empty
                   )
  extends ProcessTask
    with HasForm
    with HasInFlows
    with HasOutFlows {

  def groups(): Seq[Group] = candidateGroups.groups

  def users(): Seq[User] = candidateUsers.users

  def candidateGroups(candGroups: Group*): UserTask = copy(candidateGroups = candidateGroups ++ candGroups)

  def candidateGroup(group: Group): UserTask = candidateGroups(group)

  def candidateUsers(users: User*): UserTask = copy(candidateUsers = candidateUsers ++ users)

  def candidateUser(user: User): UserTask = candidateUsers(user)

}

//<businessRuleTask id="CountryRiskTask" name="Country Risk" camunda:asyncBefore="true" camunda:asyncAfter="true" camunda:resultVariable="approvalRequired" camunda:decisionRef="country-risk" camunda:mapDecisionResult="singleEntry">
// only implementation DMN supported
case class BusinessRuleTask(id: BpmnNodeId,
                            implementation: TaskImplementation = DmnImpl.notImplemented,
                            extProperties: ExtProperties = ExtProperties.none,
                            extInOutputs: ExtInOutputs = ExtInOutputs.none,
                            inFlows: Seq[SequenceFlow] = Seq.empty,
                            outFlows: Seq[SequenceFlow] = Seq.empty
                           )
  extends ImplementationTask
    with HasInFlows
    with HasOutFlows {

  override def generateDsl(): String =
    super.generateDsl() +
      generateInFlowDsl +
      generateOutFlowDsl

  def dmn(decisionRef: FilePath, resultVariable: Identifier = "ruleResult"): BusinessRuleTask = copy(implementation =
    DmnImpl(StaticFile(decisionRef), resultVariable))

}

case class CallActivity(id: BpmnNodeId,
                        calledElement: CalledBpmn = CalledBpmn.notImplemented,
                        extProperties: ExtProperties = ExtProperties.none,
                        extInOutputs: ExtInOutputs = ExtInOutputs.none,
                        extInOuts: ExtCallActivityInOuts = ExtCallActivityInOuts.none,
                        inFlows: Seq[SequenceFlow] = Seq.empty,
                        outFlows: Seq[SequenceFlow] = Seq.empty
                       )
  extends ProcessTask
    with HasInFlows
    with HasOutFlows {

  def calledElement(process: BpmnProcess): CallActivity =
    copy(calledElement = CalledBpmn(process))

  def in(source: PropKey, target: PropKey): CallActivity =
    copy(extInOuts = extInOuts :+ extInOuts.in(source, target))

  def out(source: PropKey, target: PropKey): CallActivity =
    copy(extInOuts = extInOuts :+ extInOuts.out(source, target))

  def inExpressionFromJsonPath(path: JsonPath, target: PropKey): CallActivity =
    copy(extInOuts = extInOuts :+ extInOuts.inExpressionFromJsonPath(path, target))

  def outExpressionFromJsonPath(path: JsonPath, target: PropKey): CallActivity =
    copy(extInOuts = extInOuts :+ extInOuts.outExpressionFromJsonPath(path, target))

}

case class CalledBpmn(process: BpmnProcess,
                      decisionRefBinding: String = "deployment" // only supported
                     ) {

}

object CalledBpmn {
  def notImplemented: CalledBpmn = CalledBpmn(BpmnProcess("NotImplemented"))
}
