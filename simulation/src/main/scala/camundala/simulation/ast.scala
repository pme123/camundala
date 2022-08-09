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

sealed trait SScenario:
  def name: String
  def inOut: InOut[_, _, _]
  def isIgnored: Boolean

case class ProcessScenario(
    name: String,
    process: Process[_, _],
    steps: List[SStep] = List.empty,
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None
) extends SScenario,
      WithTestOverrides[ProcessScenario]:
  def inOut: InOut[_, _, _] = process

  def add(testOverride: TestOverride): ProcessScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: ProcessScenario = copy(isIgnored = true)

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
    inOut: Process[_, _],
    status: Int,
    errorMsg: Option[String],
    isIgnored: Boolean = false
) extends SScenario

case class IncidentScenario(
    name: String,
    inOut: Process[_, _],
    incidentMsg: String,
    isIgnored: Boolean = false
) extends SScenario

sealed trait SStep:
  def name: String

sealed trait SInOutStep extends SStep, WithTestOverrides[SInOutStep]:
  lazy val inOutDescr: InOutDescr[_, _] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] | String = inOutDescr.descr
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
    inOut: Process[_, _],
    steps: List[SStep],
    testOverrides: Option[TestOverrides] = None
) extends SInOutStep:

  def add(testOverride: TestOverride): SSubProcess =
    copy(testOverrides = addOverride(testOverride))

case class SReceiveMessageEvent(
    name: String,
    inOut: ReceiveMessageEvent[_],
    readyVariable: Option[String] = None,
    readyValue: Any = true,
    processInstanceId: Boolean = true,
    testOverrides: Option[TestOverrides] = None
) extends SInOutStep:

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
) extends SInOutStep:

  def add(testOverride: TestOverride): SReceiveSignalEvent =
    copy(testOverrides = addOverride(testOverride))

case class SWaitTime(seconds: Int = 5) extends SStep:
  val name: String = s"Wait for $seconds seconds"
