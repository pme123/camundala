package camundala
package bpmn

import camundala.domain.*

import java.time.{LocalDate, LocalDateTime, ZonedDateTime}

case class InOutDescr[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema
](
    id: String,
    in: In = NoInput(),
    out: Out = NoOutput(),
    descr: Option[String] = None
)

trait Activity[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
    T <: InOut[In, Out, T]
] extends InOut[In, Out, T]

enum InOutType:
  case Bpmn, Dmn, Worker, Timer, Signal, Message, UserTask

trait InOut[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
    T <: InOut[In, Out, T]
] extends ProcessElement:
  def inOutDescr: InOutDescr[In, Out]
  // def constructor: InOutDescr[In, Out] => T
  def inOutType: InOutType

  lazy val id: String = inOutDescr.id
  lazy val descr: Option[String] = inOutDescr.descr
  lazy val in: In = inOutDescr.in
  lazy val out: Out = inOutDescr.out
  def camundaInMap: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(in)
  lazy val camundaOutMap: Map[String, CamundaVariable] =
    CamundaVariable.toCamunda(out)
  def camundaToCheckMap: Map[String, CamundaVariable] = camundaOutMap

  def withInOutDescr(inOutDescr: InOutDescr[In, Out]): T

  def withId(i: String): T =
    withInOutDescr(inOutDescr.copy(id = i))

  def withDescr(description: String): T =
    withInOutDescr(inOutDescr.copy(descr = Some(description)))

  def withIn(in: In): T =
    withInOutDescr(inOutDescr.copy(in = in))

  // this allows you to manipulate the existing in directly
  def withIn(inFunct: In => In): T =
    withInOutDescr(inOutDescr.copy(in = inFunct(in)))

  def withOut(out: Out): T =
    withInOutDescr(
      inOutDescr.copy(out = out)
    )

  // this allows you to manipulate the existing out directly
  def withOut(outFunct: Out => Out): T =
    withInOutDescr(inOutDescr.copy(out = outFunct(out)))
end InOut

trait ProcessElement extends Product:
  def id: String
  def typeName: String = getClass.getSimpleName
  def label: String = typeName.head.toString.toLowerCase + typeName.tail
  def descr: Option[String]
end ProcessElement

trait ProcessNode extends ProcessElement

sealed trait ProcessOrExternalTask[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
    T <: InOut[In, Out, T]
] extends InOut[In, Out, T]:
  def processName: String

  def topicName: String = processName

  protected def servicesMocked: Boolean
  protected def outputMock: Option[Out]
  protected def impersonateUserId: Option[String]

  lazy val inputVariableNames: Seq[String] = in.productElementNames.toSeq

  override def camundaInMap: Map[String, CamundaVariable] =
    val camundaOutputMock: Map[String, CamundaVariable] = outputMock
      .map(m =>
        InputParams.outputMock.toString -> CamundaVariable.valueToCamunda(
          m.asJson
        )
      )
      .toMap

    val camundaServicesMocked: (String, CamundaVariable) =
      InputParams.servicesMocked.toString -> CamundaVariable.valueToCamunda(
        servicesMocked
      )
    val camundaImpersonateUserId = impersonateUserId.toSeq.map { uiId =>
      InputParams.impersonateUserId.toString -> CamundaVariable.valueToCamunda(uiId)
    }.toMap
    super.camundaInMap ++ camundaImpersonateUserId ++ camundaOutputMock + camundaServicesMocked
  end camundaInMap
end ProcessOrExternalTask

case class Process[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
](
    inOutDescr: InOutDescr[In, Out],
    protected val elements: Seq[ProcessNode | InOut[?, ?, ?]] = Seq.empty,
    startEventType: StartEventType = StartEventType.None,
    protected val servicesMocked: Boolean = false,
    protected val mockedWorkers: Seq[String] = Seq.empty,
    protected val outputMock: Option[Out] = None,
    protected val impersonateUserId: Option[String] = None
) extends ProcessOrExternalTask[In, Out, Process[In, Out]]:
  lazy val inOutType: InOutType = InOutType.Bpmn

  lazy val processName = inOutDescr.id

  def inOuts: Seq[InOut[?, ?, ?]] = elements.collect { case io: InOut[?, ?, ?] =>
    io
  }

  def withInOutDescr(descr: InOutDescr[In, Out]): Process[In, Out] =
    copy(inOutDescr = descr)

  def withElements(elements: (ProcessNode | InOut[?, ?, ?])*): Process[In, Out] =
    this.copy(elements = elements)

  def withImpersonateUserId(impersonateUserId: String): Process[In, Out] =
    copy(impersonateUserId = Some(impersonateUserId))

  def withStartEventType(startEventType: StartEventType): Process[In, Out] =
    copy(startEventType = startEventType)

  def mockServices: Process[In, Out] =
    copy(servicesMocked = true)

  def mockWith(outputMock: Out): Process[In, Out] =
    copy(outputMock = Some(outputMock))

  def mockWorkers(workerNames: String*): Process[In, Out] =
    copy(mockedWorkers = workerNames)

  def mockWorker(workerName: String): Process[In, Out] =
    copy(mockedWorkers = mockedWorkers :+ workerName)

  override def camundaInMap: Map[String, CamundaVariable] =
    val camundaMockedWorkers =
      InputParams.mockedWorkers.toString -> CamundaVariable.valueToCamunda(
        mockedWorkers.asJson
      )

    super.camundaInMap + camundaMockedWorkers
  end camundaInMap

end Process

enum StartEventType:
  case None, Message, Signal

object StartEventType:
  given InOutCodec[StartEventType] = deriveCodec
  given ApiSchema[StartEventType] = deriveApiSchema

sealed trait ExternalTask[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
    T <: ExternalTask[In, Out, T]
] extends ProcessOrExternalTask[In, Out, T]:
  override final def topicName: String = inOutDescr.id
  protected def manualOutMapping: Boolean
  protected def outputVariables: Seq[String]
  protected def handledErrors: Seq[ErrorCodeType]
  protected def regexHandledErrors: Seq[String]
  lazy val inOutType: InOutType = InOutType.Worker

  def processName: String = GenericExternalTaskProcessName

  override def camundaInMap: Map[String, CamundaVariable] =
    super.camundaInMap +
      (InputParams.handledErrors.toString -> CamundaVariable.valueToCamunda(
        handledErrors.map(_.toString).asJson
      )) +
      (InputParams.regexHandledErrors.toString -> CamundaVariable
        .valueToCamunda(regexHandledErrors.asJson)) +
      (InputParams.topicName.toString -> CamundaVariable
        .valueToCamunda(topicName)) +
      (InputParams.manualOutMapping.toString -> CamundaVariable
        .valueToCamunda(manualOutMapping)) +
      (InputParams.outputVariables.toString -> CamundaVariable
        .valueToCamunda(outputVariables.asJson))
end ExternalTask

case class ServiceTask[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema,
    ServiceIn: InOutEncoder: InOutDecoder,
    ServiceOut: InOutEncoder: InOutDecoder
](
    inOutDescr: InOutDescr[In, Out],
    defaultServiceOutMock: MockedServiceResponse[ServiceOut],
    serviceInExample: ServiceIn,
    @deprecated(
      "Default is _GenericExternalTaskProcessName_ - in future only used as External Task"
    )
    override val processName: String = GenericExternalTaskProcessName,
    protected val outputMock: Option[Out] = None,
    protected val servicesMocked: Boolean = false,
    protected val outputServiceMock: Option[MockedServiceResponse[ServiceOut]] = None,
    protected val outputVariables: Seq[String] = Seq.empty,
    protected val manualOutMapping: Boolean = false,
    protected val handledErrors: Seq[ErrorCodeType] = Seq.empty,
    protected val regexHandledErrors: Seq[String] = Seq.empty,
    protected val impersonateUserId: Option[String] = None
) extends ExternalTask[In, Out, ServiceTask[In, Out, ServiceIn, ServiceOut]]:

  @deprecated("Use _topicName_")
  lazy val serviceName: String = inOutDescr.id

  def withInOutDescr(
      descr: InOutDescr[In, Out]
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(inOutDescr = descr)

  def withProcessName(
      processName: String
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(processName = processName)

  def mockWith(outputMock: Out): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(outputMock = Some(outputMock))

  def mockServicesWithDefault: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(servicesMocked = true)

  def mockServiceWith(
      outputServiceMock: MockedServiceResponse[ServiceOut]
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(outputServiceMock = Some(outputServiceMock))

  // shortcut for success case
  def mockServiceWith(
      outputServiceMock: ServiceOut
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(outputServiceMock = Some(MockedServiceResponse.success200(outputServiceMock)))

  def withOutputVariables(names: String*): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(outputVariables = names)

  def withOutputVariable(
      processName: String
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(outputVariables = outputVariables :+ processName)

  def withManualOutMapping: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(manualOutMapping = true)

  def handleErrors(
      errorCodes: ErrorCodeType*
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(handledErrors = errorCodes)

  def handleError(
      errorCode: ErrorCodeType
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(handledErrors = handledErrors :+ errorCode)

  def handleErrorWithRegex(
      regex: String
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(regexHandledErrors = regexHandledErrors :+ regex)

  def withImpersonateUserId(
      impersonateUserId: String
  ): ServiceTask[In, Out, ServiceIn, ServiceOut] =
    copy(impersonateUserId = Some(impersonateUserId))

  override def camundaInMap: Map[String, CamundaVariable] =
    val camundaOutputServiceMock = outputServiceMock
      .map(m =>
        InputParams.outputServiceMock.toString -> CamundaVariable.valueToCamunda(
          m.asJson
        )
      )
      .toMap
    super.camundaInMap ++ camundaOutputServiceMock
  end camundaInMap

end ServiceTask

case class CustomTask[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema
](
    inOutDescr: InOutDescr[In, Out],
    protected val outputMock: Option[Out] = None,
    protected val outputVariables: Seq[String] = Seq.empty,
    protected val servicesMocked: Boolean = false,
    protected val manualOutMapping: Boolean = false,
    protected val impersonateUserId: Option[String] = None,
    protected val handledErrors: Seq[ErrorCodeType] = Seq.empty,
    protected val regexHandledErrors: Seq[String] = Seq.empty
) extends ExternalTask[In, Out, CustomTask[In, Out]]:

  def withInOutDescr(descr: InOutDescr[In, Out]): CustomTask[In, Out] =
    copy(inOutDescr = descr)

  def withImpersonateUserId(impersonateUserId: String): CustomTask[In, Out] =
    copy(impersonateUserId = Some(impersonateUserId))

  def mockServices: CustomTask[In, Out] =
    copy(servicesMocked = true)

  def mockWith(outputMock: Out): CustomTask[In, Out] =
    copy(outputMock = Some(outputMock))

  def withOutputVariables(names: String*): CustomTask[In, Out] =
    copy(outputVariables = names)

  def withOutputVariable(name: String): CustomTask[In, Out] =
    withOutputVariables(name)

  def handleErrors(
      errorCodes: ErrorCodeType*
  ): CustomTask[In, Out] =
    copy(handledErrors = errorCodes)

  def handleError(
      errorCode: ErrorCodeType
  ): CustomTask[In, Out] =
    copy(handledErrors = handledErrors :+ errorCode)

  def handleErrorWithRegex(
      regex: String
  ): CustomTask[In, Out] =
    copy(regexHandledErrors = regexHandledErrors :+ regex)

end CustomTask

case class UserTask[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    Out <: Product: InOutEncoder: InOutDecoder: Schema
](
    inOutDescr: InOutDescr[In, Out]
) extends ProcessNode,
      Activity[In, Out, UserTask[In, Out]]:
  lazy val inOutType: InOutType = InOutType.UserTask

  override lazy val camundaToCheckMap: Map[String, CamundaVariable] =
    camundaInMap

  def withInOutDescr(descr: InOutDescr[In, Out]): UserTask[In, Out] =
    copy(inOutDescr = descr)
end UserTask

object UserTask:

  def init(id: String): UserTask[NoInput, NoOutput] =
    UserTask(
      InOutDescr(id, NoInput(), NoOutput())
    )
end UserTask

sealed trait ReceiveEvent[
    In <: Product: InOutEncoder: InOutDecoder: Schema,
    T <: ReceiveEvent[In, T]
] extends ProcessNode,
      Activity[In, NoOutput, T]

case class MessageEvent[
    In <: Product: InOutEncoder: InOutDecoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ReceiveEvent[In, MessageEvent[In]]:
  lazy val inOutType: InOutType = InOutType.Message

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): MessageEvent[In] =
    copy(inOutDescr = descr)
end MessageEvent

object MessageEvent:

  def init(id: String): MessageEvent[NoInput] =
    MessageEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )
end MessageEvent

case class SignalEvent[
    In <: Product: InOutEncoder: InOutDecoder: Schema
](
    messageName: String,
    inOutDescr: InOutDescr[In, NoOutput]
) extends ReceiveEvent[In, SignalEvent[In]]:
  lazy val inOutType: InOutType = InOutType.Signal

  def withInOutDescr(descr: InOutDescr[In, NoOutput]): SignalEvent[In] =
    copy(inOutDescr = descr)
end SignalEvent

object SignalEvent:

  def init(id: String): SignalEvent[NoInput] =
    SignalEvent(
      id,
      InOutDescr(id, NoInput(), NoOutput())
    )
end SignalEvent

case class TimerEvent(
    title: String,
    inOutDescr: InOutDescr[NoInput, NoOutput]
) extends ReceiveEvent[NoInput, TimerEvent]:
  lazy val inOutType: InOutType = InOutType.Timer

  def withInOutDescr(descr: InOutDescr[NoInput, NoOutput]): TimerEvent =
    copy(inOutDescr = descr)
end TimerEvent

object TimerEvent:

  def init(title: String): TimerEvent =
    TimerEvent(title, InOutDescr(title, NoInput(), NoOutput()))
end TimerEvent

def valueToJson(value: Any): Json =
  value match
    case v: Int =>
      Json.fromInt(v)
    case v: Long =>
      Json.fromLong(v)
    case v: Boolean =>
      Json.fromBoolean(v)
    case v: Float =>
      Json.fromFloat(v).getOrElse(Json.Null)
    case v: Double =>
      Json.fromDouble(v).getOrElse(Json.Null)
    case null =>
      Json.Null
    case ld: LocalDate =>
      Json.fromString(ld.toString)
    case ldt: LocalDateTime =>
      Json.fromString(ldt.toString)
    case zdt: ZonedDateTime =>
      Json.fromString(zdt.toString)
    case v =>
      Json.fromString(v.toString)
end valueToJson
