package camundala
package simulation

import camundala.bpmn.*

case class SSimulation(scenarios: List[SScenario])

sealed trait WithTestOverrides[T <: WithTestOverrides[T]]:
  def inOut: InOut[?, ?, ?]
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
  def inOut: InOut[?, ?, ?]
  def isIgnored: Boolean
  def ignored: SScenario
  def withSteps(steps: List[SStep]): SScenario

sealed trait HasProcessSteps extends ScenarioOrStep:
  def process: ProcessOrService[?, ?, ?]
  def steps: List[SStep]

sealed trait IsProcessScenario extends HasProcessSteps, SScenario

case class ProcessScenario(
    // this is name of process in case of START
    // this is message name in case of MESSAGE
    // this is signal name in case of SIGNAL
    name: String,
    process: Process[?, ?],
    steps: List[SStep] = List.empty,
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None,
    startType: ProcessStartType = ProcessStartType.START
) extends IsProcessScenario,
      WithTestOverrides[ProcessScenario]:
  def inOut: InOut[?, ?, ?] = process

  def add(testOverride: TestOverride): ProcessScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: ProcessScenario = copy(isIgnored = true)
  def withSteps(steps: List[SStep]): SScenario =
    copy(steps = steps)
end ProcessScenario

enum ProcessStartType:
  case START, MESSAGE

case class ServiceProcessScenario(
    name: String,
    process: ServiceProcess[?, ?, ?, ?],
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None,
    startType: ProcessStartType = ProcessStartType.START
) extends IsProcessScenario,
      WithTestOverrides[ServiceProcessScenario]:

  lazy val steps: List[SStep] = List.empty
  def inOut: InOut[?, ?, ?] = process

  def add(testOverride: TestOverride): ServiceProcessScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: ServiceProcessScenario = copy(isIgnored = true)

  def withSteps(steps: List[SStep]): SScenario =
    this

end ServiceProcessScenario

case class DmnScenario(
    name: String,
    inOut: DecisionDmn[?, ?],
    isIgnored: Boolean = false,
    testOverrides: Option[TestOverrides] = None
) extends SScenario,
      WithTestOverrides[DmnScenario]:
  def add(testOverride: TestOverride): DmnScenario =
    copy(testOverrides = addOverride(testOverride))

  def ignored: DmnScenario = copy(isIgnored = true)

  def withSteps(steps: List[SStep]): SScenario =
    this
end DmnScenario

case class BadScenario(
    name: String,
    process: Process[?, ?],
    status: Int,
    errorMsg: Option[String],
    isIgnored: Boolean = false
) extends IsProcessScenario:
  lazy val inOut: Process[?, ?] = process
  lazy val steps: List[SStep] = List.empty
  def ignored: BadScenario = copy(isIgnored = true)

  def withSteps(steps: List[SStep]): SScenario =
    this
end BadScenario

trait IsIncidentScenario extends IsProcessScenario:
  def incidentMsg: String

case class IncidentScenario(
    name: String,
    process: Process[?, ?],
    steps: List[SStep] = List.empty,
    incidentMsg: String,
    isIgnored: Boolean = false
) extends IsIncidentScenario:
  lazy val inOut: Process[?, ?] = process

  def ignored: IncidentScenario = copy(isIgnored = true)

  def withSteps(steps: List[SStep]): SScenario =
    copy(steps = steps)

end IncidentScenario

case class IncidentServiceScenario(
    name: String,
    process: ServiceProcess[?, ?, ?, ?],
    incidentMsg: String,
    isIgnored: Boolean = false
) extends IsIncidentScenario:
  lazy val inOut: ServiceProcess[?, ?, ?, ?] = process
  lazy val steps: List[SStep] = List.empty

  def ignored: IncidentServiceScenario = copy(isIgnored = true)

  def withSteps(steps: List[SStep]): SScenario = this

end IncidentServiceScenario

sealed trait SStep extends ScenarioOrStep

sealed trait SInServiceOuttep
    extends SStep,
      WithTestOverrides[SInServiceOuttep]:
  lazy val inOutDescr: InOutDescr[?, ?] = inOut.inOutDescr
  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] = inOutDescr.descr
  lazy val camundaInMap: Map[String, CamundaVariable] = inOut.camundaInMap
  lazy val camundaOutMap: Map[String, CamundaVariable] = inOut.camundaOutMap

case class SUserTask(
    name: String,
    inOut: UserTask[?, ?],
    testOverrides: Option[TestOverrides] = None,
    // after getting a task, you can wait - used for intermediate events running something.
    waitForSec: Option[Int] = None
) extends SInServiceOuttep:

  def add(testOverride: TestOverride): SUserTask =
    copy(testOverrides = addOverride(testOverride))

case class SSubProcess(
    name: String,
    process: Process[?, ?],
    steps: List[SStep],
    testOverrides: Option[TestOverrides] = None
) extends SInServiceOuttep,
      HasProcessSteps:

  lazy val processName: String = process.processName
  lazy val inOut: Process[?, ?] = process

  def add(testOverride: TestOverride): SSubProcess =
    copy(testOverrides = addOverride(testOverride))

sealed trait SEvent extends SInServiceOuttep:
  def readyVariable: String
  def readyValue: Any

case class SMessageEvent(
    name: String,
    inOut: MessageEvent[?],
    optReadyVariable: Option[String] = None,
    readyValue: Any = true,
    processInstanceId: Boolean = true,
    testOverrides: Option[TestOverrides] = None
) extends SEvent:
  lazy val readyVariable: String = optReadyVariable.getOrElse(notSet)

  def add(testOverride: TestOverride): SMessageEvent =
    copy(testOverrides = addOverride(testOverride))

  // If you send a Message to start a process, there is no processInstanceId
  def start: SMessageEvent =
    copy(processInstanceId = false)

case class SSignalEvent(
    name: String,
    inOut: SignalEvent[?],
    readyVariable: String = "waitForSignal",
    readyValue: Any = true,
    testOverrides: Option[TestOverrides] = None
) extends SEvent:

  def add(testOverride: TestOverride): SSignalEvent =
    copy(testOverrides = addOverride(testOverride))

end SSignalEvent

case class STimerEvent(
    name: String,
    inOut: TimerEvent,
    optReadyVariable: Option[String] = None,
    readyValue: Any = true,
    testOverrides: Option[TestOverrides] = None
) extends SEvent:
  lazy val readyVariable: String = optReadyVariable.getOrElse(notSet)

  def add(testOverride: TestOverride): STimerEvent =
    copy(testOverrides = addOverride(testOverride))

end STimerEvent

case class SWaitTime(seconds: Int = 5) extends SStep:
  val name: String = s"Wait for $seconds seconds"

lazy val notSet = "NotSet"
