package camundala.worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import zio.*
import zio.test.*
import zio.test.Assertion.*
import zio.test.TestAspect.*

import scala.reflect.ClassTag

object WorkerExecutorSpec extends ZIOSpecDefault:

  def descr: String = "myDescr"

  given EngineRunContext = EngineRunContext(
    new EngineContext:
      override def getLogger(clazz: Class[?]): WorkerLogger = ???
      override def toEngineObject: Json => Any = ???
      override def sendRequest[ServiceIn: Encoder, ServiceOut: {Decoder, ClassTag}](
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

  def spec = suite("WorkerExecutorSpec")(
    test("InputValidator WithConfig override InConfig") {
      val result = executor.InputValidator.validate(Seq(
        ZIO.succeed("requiredValue" -> None),
        ZIO.succeed("optionalValue" -> None),
        ZIO.succeed("aValue" -> Some(Json.fromString("ok"))),
        ZIO.succeed("inConfig" -> Some(Json.obj("requiredValue" -> Json.fromString("aso"))))
      ))

      assertZIO(result)(
        equalTo(In(inConfig = Some(InConfig(requiredValue = "aso"))))
      )
    },

    test("InputValidator WithConfig default InConfig") {
      val result = executor.InputValidator.validate(Seq(
        ZIO.succeed("requiredValue" -> None),
        ZIO.succeed("optionalValue" -> None),
        ZIO.succeed("aValue" -> Some(Json.fromString("ok")))
      ))

      assertZIO(result)(
        equalTo(In(inConfig = Some(InConfig())))
      )
    },

    test("InputValidator WithConfig override InConfig in In") {
      val result = executor.InputValidator.validate(Seq(
        ZIO.succeed("aValue" -> Some(Json.fromString("ok"))),
        ZIO.succeed("requiredValue" -> Some(Json.fromString("aso"))),
        ZIO.succeed("optionalValue" -> Some(Json.fromString("nei")))
      ))

      assertZIO(result)(
        equalTo(In(inConfig = Some(InConfig(requiredValue = "aso", optionalValue = Some("nei")))))
      )
    },

    test("Test optional values are null in JSON") {
      val in = (ZIO.fromEither(InConfig().asJson.hcursor
        .downField("optionalValue")
        .as[Json]))
      val out = Json.Null

      assertZIO(in)(equalTo(out))
    }
  ) @@ sequential

end WorkerExecutorSpec
