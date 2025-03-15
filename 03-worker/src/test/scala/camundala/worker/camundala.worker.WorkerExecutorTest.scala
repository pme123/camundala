package camundala.worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import zio.ZIO

import scala.reflect.ClassTag

class WorkerExecutorTest extends munit.FunSuite:

  def descr: String = "myDescr"

  given EngineRunContext = EngineRunContext(
    new EngineContext:
      override def getLogger(clazz: Class[?]): WorkerLogger = ???
      override def toEngineObject: Json => Any = ???
      override def sendRequest[ServiceIn: Encoder, ServiceOut: Decoder: ClassTag](
          request: RunnableRequest[ServiceIn]
      ): SendRequestType[ServiceOut] = ???
    ,
    GeneralVariables()
  )

  def processName: String = "test-process"
  def example =
    Process(InOutDescr(processName, In(), NoOutput()), NoInput(), ProcessLabels.none)
  import In.given
  def worker = InitWorker(example)

  case class In(aValue: String = "ok", inConfig: Option[InConfig] = None)
      extends WithConfig[InConfig]:
    def defaultConfig: InConfig = InConfig()

  object In:
    given InOutCodec[In] = deriveInOutCodec[In]
    given ApiSchema[In] = deriveApiSchema[In]

  case class InConfig(
      requiredValue: String = "required",
      optionalValue: Option[String] = None
  )

  object InConfig:
    given InOutCodec[InConfig] = deriveInOutCodec[InConfig]
    given ApiSchema[InConfig] = deriveApiSchema[InConfig]

  lazy val executor = WorkerExecutor(worker)

  test("InputValidator WithConfig override InConfig"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        ZIO.succeed("requiredValue" -> None),
        ZIO.succeed("optionalValue" -> None),
        ZIO.succeed("aValue" -> Some(Json.fromString("ok"))),
        ZIO.succeed("inConfig" -> Some(Json.obj("requiredValue" -> Json.fromString("aso"))))
      )),
      ZIO.succeed(In(inConfig = Some(InConfig(requiredValue = "aso"))))
    )
  test("InputValidator WithConfig default InConfig"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        ZIO.succeed("requiredValue" -> None),
        ZIO.succeed("optionalValue" -> None),
        ZIO.succeed("aValue" -> Some(Json.fromString("ok")))
      )),
      ZIO.succeed(In(inConfig = Some(InConfig())))
    )
  test("InputValidator WithConfig override InConfig in In"):
    assertEquals(
      executor.InputValidator.validate(Seq(
        ZIO.succeed("aValue" -> Some(Json.fromString("ok"))),
        ZIO.succeed("requiredValue" -> Some(Json.fromString("aso"))),
        ZIO.succeed("optionalValue" -> Some(Json.fromString("nei")))
      )),
      ZIO.succeed(In(inConfig = Some(InConfig(requiredValue = "aso", optionalValue = Some("nei")))))
    )

  test("Test optional values are null in JSON"):
    val in = InConfig().asJson.hcursor
      .downField("optionalValue")
      .as[Json]
    val out = Json.Null
    assertEquals(
      in,
      Right(out)
    )
 
end WorkerExecutorTest
