package camundala.worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*

import scala.reflect.ClassTag

class WorkerExecutorTest extends munit.FunSuite, BpmnProcessDsl:

  def descr: String = "myDescr"

  given EngineRunContext = EngineRunContext(
    new EngineContext:
      override def getLogger(clazz: Class[?]): WorkerLogger = ???
      override def toEngineObject: Json => Any              = ???
      override def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder: ClassTag](
          request: RunnableRequest[ServiceIn]
      ): SendRequestType[ServiceOut] = ???
    ,
    GeneralVariables()
  )

  def processName: String = "test-process"
  def example             = process(In(), NoOutput())
  import In.given
  def worker              = InitWorker(example)

  case class In(aValue: String = "ok", inConfig: Option[InConfig] = None)
      extends WithConfig[InConfig]:
    def defaultConfig: InConfig = InConfig()

  object In:
    given InOutCodec[In] = deriveInOutCodec[In]
    given ApiSchema[In]  = deriveApiSchema[In]

  case class InConfig(
      requiredValue: String = "required",
      optionalValue: Option[String] = None
  )

  object InConfig:
    given InOutCodec[InConfig] = deriveInOutCodec[InConfig]
    given ApiSchema[InConfig]  = deriveApiSchema[InConfig]

  lazy val executor = WorkerExecutor(worker)

  test("InputValidator WithConfig override InConfig"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        Right("requiredValue" -> None),
        Right("optionalValue" -> None),
        Right("aValue"        -> Some(Json.fromString("ok"))),
        Right("inConfig"      -> Some(Json.obj("requiredValue" -> Json.fromString("aso"))))
      )),
      Right(In(inConfig = Some(InConfig(requiredValue = "aso"))))
    )
  test("InputValidator WithConfig default InConfig"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        Right("requiredValue" -> None),
        Right("optionalValue" -> None),
        Right("aValue"        -> Some(Json.fromString("ok")))
      )),
      Right(In(inConfig = Some(InConfig())))
    )
  test("InputValidator WithConfig override InConfig in In"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        Right("aValue"        -> Some(Json.fromString("ok"))),
        Right("requiredValue" -> Some(Json.fromString("aso"))),
        Right("optionalValue" -> Some(Json.fromString("nei")))
      )),
      Right(In(inConfig = Some(InConfig(requiredValue = "aso", optionalValue = Some("nei")))))
    )

  test("Test optional values are null in JSON"):
    val in  = InConfig().asJson.hcursor
      .downField("optionalValue")
      .as[Json]
    val out = Json.Null
    assertEquals(
      in,
      Right(out)
    )

end WorkerExecutorTest
