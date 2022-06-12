package camundala
package bpmn

case class BpmnProcesses(processes: bpmn.Process[?,?]*):

  def :+(process: bpmn.Process[?,?]): BpmnProcesses = BpmnProcesses(
    processes :+ process:_*
  )

object BpmnProcesses:
  def none = new BpmnProcesses()
/*
case class BpmnProcess(
                        ident: Ident,
                        descr: Option[String] = None,
                        starterGroups: CandidateGroups = CandidateGroups.none,
                        starterUsers: CandidateUsers = CandidateUsers.none,
                        processNodes: ProcessNodes = ProcessNodes.none,
                        flows: SequenceFlows = SequenceFlows.none,
                        inputObject: InOutObject = InOutObject.none,
                        outputObject: InOutObject = InOutObject.none
) extends HasGroups[BpmnProcess],
      HasInputObject[BpmnProcess],
      HasOutputObject[BpmnProcess]:

  val identStr = ident.toString
  val elements = processNodes.elements ++ flows.elements

  def withInput(input: InOutObject): BpmnProcess =
    copy(inputObject = input)

  def withOutput(output: InOutObject): BpmnProcess =
    copy(outputObject = output)

sealed trait ElemKey:
  def name: String

  def order: Int

object ElemKey:

  case object startEvents extends ElemKey:
    val name = "startEvent"
    val order = 1

    override def toString: String = "startEvents"

  case object userTasks extends ElemKey:
    val name = "userTask"
    val order = 2

    override def toString: String = "userTasks"

  case object serviceTasks extends ElemKey:
    val name = "serviceTask"
    val order = 3

    override def toString: String = "serviceTasks"

  case object businessRuleTasks extends ElemKey:
    val name = "businessRuleTask"
    val order = 4

    override def toString: String = "businessRuleTasks"

  case object sendTasks extends ElemKey:
    val name = "sendTask"
    val order = 5

    override def toString: String = "sendTasks"

  case object callActivities extends ElemKey:
    val name = "callActivity"
    val order = 6

    override def toString: String = "callActivities"

  case object exclusiveGateways extends ElemKey:
    val name = "exclusiveGateway"
    val order = 7

    override def toString: String = "exclusiveGateways"

  case object parallelGateways extends ElemKey:
    val name = "parallelGateway"
    val order = 7

    override def toString: String = "parallelGateways"

  case object endEvents extends ElemKey:
    val name = "endEvent"
    val order = 8

    override def toString: String = "endEvents"

  case object sequenceFlows extends ElemKey:
    val name = "sequenceFlow"
    val order = 9

    override def toString: String = "flows"

trait ProcessElements:

  def elements: Seq[HasProcessElement[_]]

type ProcessNodeType = StartEvent | UserTask | ServiceTask | ScriptTask |
  CallActivity | BusinessRuleTask | ParallelGateway | ExclusiveGateway |
  EndEvent

case class ProcessNodes(nodes: Seq[HasProcessNode[_]]) extends ProcessElements:

  val elements: Seq[HasProcessElement[_]] = nodes

  def ++(newNodes: Seq[HasProcessNode[_]]) = ProcessNodes(nodes ++ newNodes)
  def :+(newNode: HasProcessNode[_]) = ProcessNodes(nodes :+ newNode)

object ProcessNodes:

  val none = ProcessNodes(Nil)

case class ProcessElement(
    ident: Ident,
    properties: Properties = Properties.none,
    executionListeners: ExecutionListeners = ExecutionListeners.none
):

  def prop(prop: Property): ProcessElement =
    copy(properties = properties :+ prop)

  def executionListener(listener: ExecutionListener): ProcessElement =
    copy(executionListeners = executionListeners :+ listener)
  def executionListeners(listeners: Seq[ExecutionListener]): ProcessElement =
    copy(executionListeners = ExecutionListeners(listeners))

object ProcessElement:
  def apply(ident: String): ProcessElement =
    ProcessElement(Ident(ident))

trait HasProcessElement[T] extends HasProperties[T], HasExecutionListeners[T]:
  def processElement: ProcessElement
  def withProcessElement(processElement: ProcessElement): T

  def ident: Ident = processElement.ident
  def identStr: String = ident.toString
  def elemKey: ElemKey
  def ref: ProcessElementRef = ProcessElementRef(ident.toString)

  def properties: Properties = processElement.properties

  def prop(prop: Property): T = withProcessElement(processElement.prop(prop))

  def executionListeners: ExecutionListeners = processElement.executionListeners

  def withExecutionListener(listener: ExecutionListener): T =
    withProcessElement(processElement.executionListener(listener))

  def withExecutionListeners(listeners: Seq[ExecutionListener]): T =
    withProcessElement(processElement.executionListeners(listeners))

opaque type ProcessElementRef = String

object ProcessElementRef:
  def apply(ref: String): ProcessElementRef = ref

case class ProcessNode(
    processElement: ProcessElement,
    isAsyncBefore: Boolean = false,
    isAsyncAfter: Boolean = false
):

  val ident = processElement.ident
  val properties: Properties = processElement.properties

  def asyncBefore: ProcessNode = copy(isAsyncBefore = true)

  def asyncAfter: ProcessNode = copy(isAsyncAfter = true)

object ProcessNode:
  def apply(ident: String): ProcessNode =
    ProcessNode(ProcessElement(ident))

trait HasProcessNode[T]
    extends HasProcessElement[T]
    with HasTransactionBoundary[T]:
  def processNode: ProcessNode
  def withProcessNode(processNode: ProcessNode): T

  def withProcessElement(processElement: ProcessElement): T = withProcessNode(
    processNode.copy(processElement = processElement)
  )

  def processElement: ProcessElement = processNode.processElement

  def isAsyncBefore: Boolean = processNode.isAsyncBefore

  def isAsyncAfter: Boolean = processNode.isAsyncAfter

  def asyncBefore: T = withProcessNode(processNode.asyncBefore)

  def asyncAfter: T = withProcessNode(processNode.asyncAfter)
*/