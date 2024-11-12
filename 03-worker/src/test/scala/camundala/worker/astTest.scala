package camundala.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.MockedOutput

import scala.reflect.ClassTag

class astTest extends munit.FunSuite, BpmnProcessDsl, BpmnServiceTaskDsl:

  given EngineRunContext = EngineRunContext(
    new EngineContext:
      override def getLogger(clazz: Class[?]): WorkerLogger = ???

      override def toEngineObject: Json => Any =
        json => json.asBoolean.get

      override def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder: ClassTag](
          request: RunnableRequest[ServiceIn]
      ): SendRequestType[ServiceOut] = ???
    ,
    GeneralVariables()
  )

  test("defaultMock Process") {
    val proc = process(
      In(),
      Out()
    ).mockWith: in =>
      if in.value == 1 then Out(true)
      else Out(false)

    val worker: InitWorker[In, Out, In] = InitWorker(
      inOutExample = proc
    )

    assertEquals(worker.defaultMock(In(1)), Right(Out()))
    assertEquals(worker.defaultMock(In(3)), Right(Out(false)))
  }

  test("defaultMock ServiceTask") {
    val servTask = ServiceTask[In, Out, NoInput, Out](
      inOutDescr = InOutDescr[In, Out](
        id = "test",
        in = In(),
        out = Out()
      ),
      defaultServiceOutMock = MockedServiceResponse.success200(Out()),
      serviceInExample = NoInput()
    ).mockWith: in =>
      MockedServiceResponse.success200(Out(
        if in.value == 1 then true else false
      ))

    val worker = ServiceWorker(
      inOutExample = servTask,
      runWorkHandler = Some(
        ServiceHandler[In, Out, NoInput, Out](
          httpMethod = Method.GET,
          apiUri = _ => Uri("http://localhost:8080"),
          querySegments = _ => Seq.empty,
          inputMapper = _ => None,
          inputHeaders = _ => Map.empty,
          defaultServiceOutMock = servTask.defaultServiceOutMock,
          outputMapper = (serviceOut, _) => Right(Out(serviceOut.outputBody.value)),
          serviceInExample = NoInput(),
          dynamicServiceOutMock = servTask.dynamicServiceOutMock
        )
      )
    )

    assertEquals(worker.defaultMock(In(1)), Right(Out()))
    assertEquals(worker.defaultMock(In(3)), Right(Out(false)))
  }

  case class In(value: Int = 1)
  object In:
    given InOutCodec[In] = deriveInOutCodec[In]
    given ApiSchema[In] = deriveApiSchema[In]
  case class Out(value: Boolean = true)
  object Out:
    given InOutCodec[Out] = deriveInOutCodec[Out]
    given ApiSchema[Out] = deriveApiSchema[Out]

  override def processName: String = "???"
  override def descr: String = "???"
  override def companyDescr: String = "???"
  override def path: String = "???"
  override def serviceLabel: String = "???"
  override def serviceVersion: String = "???"
  override def topicName: String = "???"
end astTest
