package camundala
package simulation

import camundala.bpmn.*
import camundala.domain.Optable

import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions

trait SimulationDsl[T] extends TestOverrideExtensions:
  
  def run(sim: SSimulation): T

  class SimulationBuilder:
    private val ib = ListBuffer.empty[SScenario]

    def pushScenario(x: SScenario) = ib.append(x)

    def mkBlock = SSimulation(ib.toList)

  type SimulationConstr = SimulationBuilder ?=> Unit

  extension (scen: SScenario)
    private[simulation] def stage: SimulationConstr =
      (bldr: SimulationBuilder) ?=> bldr.pushScenario(scen)

  def simulate(body: SimulationConstr): T =
    val sb = SimulationBuilder()
    body(using sb)
    val sim = sb.mkBlock
    run(sim) // runs Scenarios

  def scenario(scen: ProcessScenario): SimulationConstr =
    scenario(scen)()

  def scenario(scen: DmnScenario): SimulationConstr =
    scen.stage

  def scenario(scen: ProcessScenario)(body: SStep*): SimulationConstr =
    scen.withSteps(body.toList).stage

  inline def badScenario(
      inline process: Process[_, _],
      status: Int,
      errorMsg: Optable[String] = None
  ): SimulationConstr =
    BadScenario(nameOfVariable(process), process, status, errorMsg.value).stage

  inline def incidentScenario(
                               inline process: Process[_, _],
                               incidentMsg: String
  )(body: SStep*): SimulationConstr =
    IncidentScenario(
      nameOfVariable(process), process, body.toList, incidentMsg).stage

  inline def incidentScenario(
                               inline process: Process[_, _],
                               incidentMsg: String
                             ): SimulationConstr =
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
    def waitFor(readyVariable: String, readyValue: Any = true) =
      SReceiveSignalEvent(rse.name, rse, readyVariable, readyValue)

  end extension

  extension (rme: ReceiveMessageEvent[_])
    def waitFor(readyVariable: String, readyValue: Any): SReceiveMessageEvent =
      SReceiveMessageEvent(rme.name, rme, Some(readyVariable), readyValue)
    def start: SReceiveMessageEvent =
      SReceiveMessageEvent(rme.name, rme).start
  end extension

  def waitFor(timeInSec: Int): SWaitTime = SWaitTime(timeInSec)

  extension (scen: ProcessScenario)
    def startWithMsg: ProcessScenario =
      scen.copy(startType = ProcessStartType.MESSAGE)

  end extension

  object ignore:

    def scenario(scen: SScenario): SimulationConstr =
      scenario(scen)()

    def scenario(scen: SScenario)(body: SStep*): SimulationConstr =
      scen.ignored.stage

    def badScenario(scen: SScenario,
                    status: Int,
                    errorMsg: Optable[String] = None): SimulationConstr =
      scenario(scen)()

    def incidentScenario(scen: SScenario,
                         incidentMsg: String): SimulationConstr =
      scenario(scen)()

    def incidentScenario(scen: SScenario,
                         incidentMsg: String)(body: SStep*): SimulationConstr =
      scenario(scen)()
  end ignore

end SimulationDsl
