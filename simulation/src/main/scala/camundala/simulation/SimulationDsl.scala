package camundala
package simulation

import camundala.bpmn.*
import camundala.domain.Optable

import scala.language.implicitConversions

trait SimulationDsl[T] extends TestOverrideExtensions:

  protected def run(sim: SSimulation): T

  def simulate(body: => SScenario*): Unit =
    try {
      run(SSimulation(body.toList)) // runs Scenarios
    }catch{
      case err => // there could be errors in the creation of the SScenarios
        err.printStackTrace()
    }
    

  def scenario(scen: ProcessScenario): SScenario =
    scenario(scen)()

  def scenario(scen: ServiceProcessScenario): SScenario =
    scen

  def scenario(scen: DmnScenario): SScenario =
    scen

  def scenario(scen: ProcessScenario)(body: SStep*): SScenario =
    scen.withSteps(body.toList)

  inline def badScenario(
      inline process: Process[?, ?],
      status: Int,
      errorMsg: Optable[String] = None
  ): BadScenario =
    BadScenario(nameOfVariable(process), process, status, errorMsg.value)

  inline def incidentScenario(
      inline process: Process[?, ?],
      incidentMsg: String
  )(body: SStep*): IncidentScenario =
    IncidentScenario(nameOfVariable(process), process, body.toList, incidentMsg)

  inline def incidentScenario(
      inline process: Process[?, ?],
      incidentMsg: String
  ): IncidentScenario =
    incidentScenario(process, incidentMsg)()

  inline def incidentScenario(
                               inline process: ServiceProcess[?, ?, ?, ?],
                               incidentMsg: String
                             ): IncidentServiceScenario =
    IncidentServiceScenario(nameOfVariable(process), process, incidentMsg)

  inline def subProcess(inline process: Process[?, ?])(
      body: SStep*
  ): SSubProcess =
    SSubProcess(nameOfVariable(process), process, body.toList)

  implicit inline def toScenario(
      inline process: Process[?, ?]
  ): ProcessScenario =
    ProcessScenario(nameOfVariable(process), process)

  implicit inline def toScenario(
      inline process: ServiceProcess[?, ?, ?, ?]
  ): ServiceProcessScenario =
    ServiceProcessScenario(nameOfVariable(process), process)

  implicit inline def toScenario(
      inline decisionDmn: DecisionDmn[?, ?]
  ): DmnScenario =
    DmnScenario(nameOfVariable(decisionDmn), decisionDmn)

  implicit inline def toStep(inline inOut: UserTask[?, ?]): SUserTask =
    SUserTask(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: MessageEvent[?]): SMessageEvent =
    SMessageEvent(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: SignalEvent[?]): SSignalEvent =
    SSignalEvent(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: TimerEvent): STimerEvent =
    STimerEvent(nameOfVariable(inOut), inOut)

  extension (event: MessageEvent[?])
    def waitFor(readyVariable: String): SMessageEvent =
      event.waitFor(readyVariable, true)
    def waitFor(readyVariable: String, readyValue: Any): SMessageEvent =
      SMessageEvent(event.name, event, Some(readyVariable), readyValue)
    def start: SMessageEvent =
      SMessageEvent(event.name, event).start
  end extension

  extension (event: SignalEvent[?])
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

    def simulate(body: SScenario*): T =
      run(SSimulation(body.map(_.ignored).toList))

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
