package camundala
package simulation

import camundala.bpmn.*
import camundala.domain.{MockedServiceResponse, Optable}

trait SimulationDsl[T] extends TestOverrideExtensions:

  protected def run(sim: SSimulation): T

  def simulate(body: => (Seq[SScenario] | SScenario)*): Unit =
    try {
      val scenarios = body.flatMap:
        case s: Seq[?] => s.collect { case ss: SScenario => ss }
        case s: SScenario => Seq(s)

      run(SSimulation(scenarios.toList)) // runs Scenarios
    } catch {
      case err => // there could be errors in the creation of the SScenarios
        err.printStackTrace()
    }

  def scenario(scen: ProcessScenario): SScenario =
    scenario(scen)()

  def scenario(scen: ExternalTaskScenario): SScenario =
    scen

  inline def serviceScenario[
    In <: Product : Encoder : Decoder : Schema,
    Out <: Product : Encoder : Decoder : Schema,
    ServiceOut: Encoder : Decoder
  ](
     task: ServiceTask[In, Out, ServiceOut],
     outputMock: Out,
     outputServiceMock: MockedServiceResponse[ServiceOut],
   ): Seq[ExternalTaskScenario] =
    val withDefaultMock = task.mockWithDefault
    val withOutputMock = task
      .mockWith(outputMock)
      .withOut(outputMock)
    val withServiceOutputMock = task
      .mockServiceWith(outputServiceMock)
      .withOut(outputMock)

    Seq(
      ExternalTaskScenario(nameOfVariable(task) + " defaultMock", withDefaultMock),
      ExternalTaskScenario(nameOfVariable(task) + " outputMock", withOutputMock),
      ExternalTaskScenario(nameOfVariable(task) + " outputServiceMock", withServiceOutputMock)
    )

  inline def serviceScenario[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema,
      ServiceOut: Encoder: Decoder
  ](
      task: ServiceTask[In, Out, ServiceOut],
      outputMock: Out,
      outputServiceMock: ServiceOut,
      respHeaders: Map[String, String] = Map.empty
  ): Seq[ExternalTaskScenario] =
    serviceScenario(task, outputMock,
      MockedServiceResponse.success200(outputServiceMock).withHeaders(respHeaders))

  inline def serviceScenario[
    In <: Product : Encoder : Decoder : Schema,
    Out <: Product : Encoder : Decoder : Schema,
    ServiceOut: Encoder : Decoder
  ](
     task: ServiceTask[In, Out, ServiceOut],
   ): Seq[ExternalTaskScenario] =
    serviceScenario(task, task.out, task.defaultServiceOutMock)

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
      inline process: ExternalTask[?, ?, ?],
      incidentMsg: String
  ): IncidentServiceScenario =
    IncidentServiceScenario(nameOfVariable(process), process, incidentMsg)

  inline def subProcess(inline process: Process[?, ?])(
      body: SStep*
  ): SSubProcess =
    SSubProcess(nameOfVariable(process), process, body.toList)

  inline given Conversion[Process[?, ?], ProcessScenario] with
    inline def apply(process: Process[?, ?]): ProcessScenario =
      ProcessScenario(nameOfVariable(process), process)

  inline given Conversion[ServiceTask[?, ?, ?], ExternalTaskScenario] with
    inline def apply(task: ServiceTask[?, ?, ? ]): ExternalTaskScenario =
      ExternalTaskScenario(nameOfVariable(task), task)

  inline given Conversion[CustomTask[?, ?], ExternalTaskScenario] with
    inline def apply(task: CustomTask[?, ? ]): ExternalTaskScenario =
      ExternalTaskScenario(nameOfVariable(task), task)

  inline given Conversion[DecisionDmn[?, ?], DmnScenario] with
    inline def apply(task: DecisionDmn[?, ? ]): DmnScenario =
      DmnScenario(nameOfVariable(task), task)

  inline given Conversion[UserTask[?, ?], SUserTask] with
    inline def apply(task: UserTask[?, ? ]): SUserTask =
      SUserTask(nameOfVariable(task), task)

  inline given Conversion[MessageEvent[?], SMessageEvent] with
    inline def apply(event: MessageEvent[? ]): SMessageEvent =
      SMessageEvent(nameOfVariable(event), event)

  inline given Conversion[SignalEvent[?], SSignalEvent] with
    inline def apply(event: SignalEvent[? ]): SSignalEvent =
      SSignalEvent(nameOfVariable(event), event)

  inline given Conversion[TimerEvent, STimerEvent] with
    inline def apply(event: TimerEvent): STimerEvent =
      STimerEvent(nameOfVariable(event), event)

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

    inline def serviceScenario[
      In <: Product : Encoder : Decoder : Schema,
      Out <: Product : Encoder : Decoder : Schema,
      ServiceOut: Encoder : Decoder
    ](
       task: ServiceTask[In, Out, ServiceOut],
       outputMock: Out,
       outputServiceMock: MockedServiceResponse[ServiceOut],
     ): Seq[ExternalTaskScenario] =
      Seq(ExternalTaskScenario(nameOfVariable(task) + " defaultMock", task).ignored)

    inline def serviceScenario[
      In <: Product : Encoder : Decoder : Schema,
      Out <: Product : Encoder : Decoder : Schema,
      ServiceOut: Encoder : Decoder
    ](
       task: ServiceTask[In, Out, ServiceOut],
       outputMock: Out,
       outputServiceMock: ServiceOut,
       respHeaders: Map[String, String] = Map.empty
     ): Seq[ExternalTaskScenario] =
      serviceScenario(task, outputMock,
        MockedServiceResponse.success200(outputServiceMock).withHeaders(respHeaders))

    inline def serviceScenario[
      In <: Product : Encoder : Decoder : Schema,
      Out <: Product : Encoder : Decoder : Schema,
      ServiceOut: Encoder : Decoder
    ](
       task: ServiceTask[In, Out, ServiceOut],
     ): Seq[ExternalTaskScenario] =
      serviceScenario(task, task.out, task.defaultServiceOutMock)

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
