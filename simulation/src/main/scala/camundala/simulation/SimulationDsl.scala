package camundala
package simulation

import camundala.bpmn.*
import camundala.domain.Optable

import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

trait SimulationDsl[T] extends TestOverrideExtensions:

  def run(sim: SSimulation): T

  def simulate(body: SScenario*): T =
    run(SSimulation(body.toList)) // runs Scenarios

  def scenario(scen: ProcessScenario): SScenario =
    scenario(scen)()

  def scenario(scen: DmnScenario): SScenario =
    scen

  def scenario(scen: ProcessScenario)(body: SStep*): SScenario =
    scen.withSteps(body.toList)

  inline def badScenario(
      inline process: Process[_, _],
      status: Int,
      errorMsg: Optable[String] = None
  ): BadScenario =
    BadScenario(nameOfVariable(process), process, status, errorMsg.value)

  inline def incidentScenario(
      inline process: Process[_, _],
      incidentMsg: String
  )(body: SStep*): IncidentScenario =
    IncidentScenario(nameOfVariable(process), process, body.toList, incidentMsg)

  inline def incidentScenario(
      inline process: Process[_, _],
      incidentMsg: String
  ): IncidentScenario =
    incidentScenario(process, incidentMsg)()

  inline def subProcess(inline process: Process[_, _])(
      body: SStep*
  ): SSubProcess =
    SSubProcess(nameOfVariable(process), process, body.toList)

  implicit inline def toScenario(
      inline process: Process[_, _]
  ): ProcessScenario =
    ProcessScenario(nameOfVariable(process), process)

  implicit inline def toScenario(
      inline decisionDmn: DecisionDmn[_, _]
  ): DmnScenario =
    DmnScenario(nameOfVariable(decisionDmn), decisionDmn)

  implicit inline def toStep(inline inOut: UserTask[_, _]): SUserTask =
    SUserTask(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: MessageEvent[_]): SMessageEvent =
    SMessageEvent(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: SignalEvent[_]): SSignalEvent =
    SSignalEvent(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: TimerEvent): STimerEvent =
    STimerEvent(nameOfVariable(inOut), inOut)

  extension (event: MessageEvent[_])
    def waitFor(readyVariable: String): SMessageEvent =
      event.waitFor(readyVariable, true)
    def waitFor(readyVariable: String, readyValue: Any): SMessageEvent =
      SMessageEvent(event.name, event, Some(readyVariable), readyValue)
    def start: SMessageEvent =
      SMessageEvent(event.name, event).start
  end extension

  extension (event: SignalEvent[_])
    def waitFor(readyVariable: String): SSignalEvent =
      event.waitFor(readyVariable, true)
    def waitFor(readyVariable: String, readyValue: Any = true): SSignalEvent =
      SSignalEvent(event.name, event, readyVariable, readyValue)
  end extension

  extension (event: TimerEvent)
    def waitFor(readyVariable: String): STimerEvent =
      event.waitFor(readyVariable, true)
    def waitFor(readyVariable: String, readyValue: Any): STimerEvent =
      STimerEvent(event.name, event, Some(readyVariable), readyValue)
  end extension

  extension (ut: UserTask[?, ?])
    def waitForSec(sec: Int): SUserTask =
      SUserTask(ut.name, ut, waitForSec = Some(sec))

  end extension

  def waitFor(timeInSec: Int): SWaitTime = SWaitTime(timeInSec)

  extension (scen: ProcessScenario)
    def startWithMsg: ProcessScenario =
      scen.copy(startType = ProcessStartType.MESSAGE)

  end extension

  object ignore:

    def scenario(scen: SScenario): SScenario =
      scen.ignored

    def scenario(scen: SScenario)(body: SStep*): SScenario =
      scen.ignored

    def badScenario(
        scen: SScenario,
        status: Int,
        errorMsg: Optable[String] = None
    ): SScenario =
      scen.ignored

    def incidentScenario(scen: SScenario, incidentMsg: String): SScenario =
      scen.ignored

    def incidentScenario(scen: SScenario, incidentMsg: String)(
        body: SStep*
    ): SScenario =
      scen.ignored
  end ignore

end SimulationDsl
