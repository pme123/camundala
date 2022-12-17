package camundala
package simulation

import api.*
import bpmn.*

import scala.annotation.targetName

case class SSimulation(scenarios: List[SScenario])

sealed trait WithTestOverrides[T <: WithTestOverrides[T]]:
  def inOut: InOut[_, _, _]
  def testOverrides: Option[TestOverrides]
  def add(testOverride: TestOverride): T
  protected def addOverride(testOverride: TestOverride): Option[TestOverrides] =
    Some(
      testOverrides
        .map(_ :+ testOverride)
        .getOrElse(TestOverrides(Seq(testOverride)))
    )
  lazy val camundaToCheckMap: Map[String, CamundaVariable] =
    inOut.camundaToCheckMap

sealed trait ScenarioOrStep:
  def name: String
  def typeName: String = getClass.getSimpleName
  
sealed trait SScenario extends ScenarioOrStep:
  def inOut: InOut[_, _, _]
  def isIgnored: Boolean

sealed trait IsProcessScenario extends SScenario:
  def process: Process[_,_]
  def steps: List[SStep]

case class ProcessScenario(
    // this is name of process in case of START
    // this is message name in case of MESSAGE
    // this is signal name in case of SIGNAL
    name: String,
    process: Process[_, _],
    steps: List[SStep] = List.empty,
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None,
    startType:ProcessStartType = ProcessStartType.START
) extends IsProcessScenario,
      WithTestOverrides[ProcessScenario]:
  def inOut: InOut[_, _, _] = process

  def add(testOverride: TestOverride): ProcessScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: ProcessScenario = copy(isIgnored = true)

enum ProcessStartType :
  case START, MESSAGE

case class DmnScenario(
    name: String,
    inOut: DecisionDmn[_, _],
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None
) extends SScenario,
      WithTestOverrides[DmnScenario]:
  def add(testOverride: TestOverride): DmnScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: DmnScenario = copy(isIgnored = true)

case class BadScenario(
    name: String,
    process: Process[_, _],
    status: Int,
    errorMsg: Option[String],
    isIgnored: Boolean = false
) extends IsProcessScenario:
  lazy val inOut: Process[_, _] = process
  lazy val steps: List[SStep] = List.empty


case class IncidentScenario(
    name: String,
    process: Process[_, _],
    steps: List[SStep] = List.empty,
    incidentMsg: String,
    isIgnored: Boolean = false
) extends IsProcessScenario:
  lazy val inOut: Process[_, _] = process

sealed trait SStep extends ScenarioOrStep

sealed trait SInOutStep extends SStep, WithTestOverrides[SInOutStep]:
  lazy val inOutDescr: InOutDescr[_, _] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] = inOutDescr.descr
  lazy val camundaInMap: Map[String, CamundaVariable] = inOut.camundaInMap
  lazy val camundaOutMap: Map[String, CamundaVariable] = inOut.camundaOutMap

case class SUserTask(
    name: String,
    inOut: UserTask[_, _],
    testOverrides: Option[TestOverrides] = None
) extends SInOutStep:

  def add(testOverride: TestOverride): SUserTask =
    copy(testOverrides = addOverride(testOverride))

case class SSubProcess(
    name: String,
    process: Process[_, _],
    steps: List[SStep],
    testOverrides: Option[TestOverrides] = None
) extends SInOutStep:

  lazy val inOut: Process[_, _] = process

  def add(testOverride: TestOverride): SSubProcess =
    copy(testOverrides = addOverride(testOverride))

sealed trait SEvent extends SInOutStep:
  def readyVariable: String
  def readyValue: Any

case class SReceiveMessageEvent(
    name: String,
    inOut: ReceiveMessageEvent[_],
    optReadyVariable: Option[String] = None,
    readyValue: Any = true,
    processInstanceId: Boolean = true,
    testOverrides: Option[TestOverrides] = None
) extends SEvent:
  lazy val readyVariable: String = optReadyVariable.getOrElse(notSet)

  def add(testOverride: TestOverride): SReceiveMessageEvent =
    copy(testOverrides = addOverride(testOverride))

  // If you send a Message to start a process, there is no processInstanceId
  def start: SReceiveMessageEvent =
    copy(processInstanceId = false)

case class SReceiveSignalEvent(
    name: String,
    inOut: ReceiveSignalEvent[_],
    readyVariable: String = "waitForSignal",
    readyValue: Any = true,
    testOverrides: Option[TestOverrides] = None
) extends SEvent:

  def add(testOverride: TestOverride): SReceiveSignalEvent =
    copy(testOverrides = addOverride(testOverride))

case class SWaitTime(seconds: Int = 5) extends SStep:
  val name: String = s"Wait for $seconds seconds"

lazy val notSet = "NotSet"
