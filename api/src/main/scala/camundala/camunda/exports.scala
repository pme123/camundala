package camundala
package camunda


export scala.jdk.CollectionConverters.*

export os.Path

export java.util.ArrayList
export java.io.FilenameFilter

export org.camunda.bpm.model.bpmn.BpmnModelInstance as CBpmnModelInstance

export org.camunda.bpm.model.xml.instance.ModelElementInstance
// does not work:
export org.camunda.bpm.model.bpmn.Bpmn as CBpmn
export org.camunda.bpm.model.bpmn.instance.{
  Process as CProcess,
  BaseElement as CBaseElement,
  CallActivity as CCallActivity,
  FlowNode as CFlowNode,
  FlowElement as CFlowElement,
  SequenceFlow as CSequenceFlow,
  StartEvent as CStartEvent,
  UserTask as CUserTask,
  ServiceTask as CServiceTask,
  SendTask as CSendTask,
  ScriptTask as CScriptTask,
  BusinessRuleTask as CBusinessRuleTask,
  ExclusiveGateway as CExclusiveGateway,
  ParallelGateway as CParallelGateway,
  EndEvent as CEndEvent,
  ConditionExpression as CConditionExpression,
}

// context function def f(using BpmnModelInstance): T
type FromCamundable[T] = CBpmnModelInstance ?=> T

trait ProjectPaths:

  def projectPath: Path
  lazy val cawemoPath: Path = projectPath / "cawemo"
  lazy val withIdPath: Path = cawemoPath / "with-ids"
  lazy val generatedPath: Path = projectPath / "src" / "main" / "resources"