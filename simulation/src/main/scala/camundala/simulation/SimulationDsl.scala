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
    IncidentScenario(
      nameOfVariable(process), process, body.toList, incidentMsg)

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
  implicit inline def toStep(inline inOut: ReceiveMessageEvent[_]): SReceiveMessageEvent =
    SReceiveMessageEvent(nameOfVariable(inOut), inOut)
  implicit inline def toStep(inline inOut: ReceiveSignalEvent[_]): SReceiveSignalEvent =
    SReceiveSignalEvent(nameOfVariable(inOut), inOut)

  extension (rse: ReceiveSignalEvent[_])
    def waitFor(readyVariable: String, readyValue: Any = true): SReceiveSignalEvent =
      SReceiveSignalEvent(rse.name, rse, readyVariable, readyValue)

  end extension

  extension (rme: ReceiveMessageEvent[_])
    def waitFor(readyVariable: String, readyValue: Any): SReceiveMessageEvent =
      SReceiveMessageEvent(rme.name, rme, Some(readyVariable), readyValue)
    def start: SReceiveMessageEvent =
      SReceiveMessageEvent(rme.name, rme).start
  end extension

  extension (ut: UserTask[?,?])
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

    def badScenario(scen: SScenario,
                    status: Int,
                    errorMsg: Optable[String] = None): SScenario =
      scen.ignored

    def incidentScenario(scen: SScenario,
                         incidentMsg: String): SScenario =
      scen.ignored

    def incidentScenario(scen: SScenario,
                         incidentMsg: String)(body: SStep*): SScenario =
      scen.ignored
  end ignore

end SimulationDsl
