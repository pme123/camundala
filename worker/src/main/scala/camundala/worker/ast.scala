package camundala
package worker

import camundala.bpmn.*
import camundala.bpmn.CamundaVariable.toCamunda
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import sttp.tapir.EndpointIO.Headers

import scala.reflect.ClassTag

case class Workers(workers: Seq[Worker[?,?,?]])

sealed trait Worker[In <: Product: CirceCodec : ClassTag, Out <: Product: CirceCodec, T <: Worker[In, Out, ?]]:
  def topic: String
  def in: In
  def out: Out
  def inValidator: InValidator[In]

  def defaultMock: Either[MockerError | MockedOutput, Option[Out]]

  def withCustomValidator(customValidator: In => Either[ValidatorError, In]): T
  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): T

  protected def variablesInit: Option[In => Either[InitializerError, Map[String, Any]]]

//TODO move functions to WorkerHandler
  def executeWorker(
                     processVariables: Seq[Either[BadVariableError, (String, Option[Json])]],
                     generalVariables: GeneralVariables,
                     jsonToCamunda: Json => Any,
                   ) =
    for {
      validatedInput <- inValidator.validate(processVariables)
      initializedInput <- initVariables(validatedInput)
      proceedOrMocked <- OutMocker(this).mockOrProceed(generalVariables, jsonToCamunda)
    } yield proceedOrMocked

  private val defaultVariables = Map(
    "serviceName" -> "NOT-USED" // serviceName is not needed anymore
  )
  private def initVariables(validatedInput: In): Either[InitializerError, Map[String, Any]] = variablesInit.map { vi =>
    vi(validatedInput).map(_ ++ defaultVariables)
  }.getOrElse(Right(defaultVariables))

end Worker

case class ProcessWorker[
  In <: Product: CirceCodec : ClassTag,
  Out <: Product: CirceCodec
](process: Process[In, Out],
  customValidator: Option[In => Either[ValidatorError, In]] = None,
  variablesInit: Option[In => Either[InitializerError, Map[String, Any]]] = None,
                         ) extends Worker[In, Out, ProcessWorker[In, Out]]:
  lazy val topic: String = process.processName
  lazy val in: In = process.in
  lazy val out: Out = process.out
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ProcessWorker[In, Out] =
    copy(customValidator = Some(validator))

  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ProcessWorker[In, Out] =
      copy(variablesInit = Some(init))

  def defaultMock: Either[MockerError | MockedOutput, Option[Out]] = Left(
    MockedOutput(toCamunda(out))
  )

end ProcessWorker

case class ServiceWorker[
  In <: Product: CirceCodec : ClassTag,
  Out <: Product: CirceCodec,
  ServiceIn <: Product: Encoder,
  ServiceOut : Decoder
](process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
  defaultHeaders:  Map[String, String] = Map.empty,
    // default is no output
  bodyOutputMapper: RequestOutput[ServiceOut] => Either[MappingError, Option[Out]] = (_:RequestOutput[ServiceOut]) => Right(None),
  customValidator: Option[In => Either[ValidatorError, In]] = None,
  variablesInit: Option[In => Either[InitializerError, Map[String, Any]]] = None,
                         ) extends Worker[In, Out, ServiceWorker[In,Out,ServiceIn, ServiceOut]]:
  lazy val topic: String = process.serviceName
  lazy val in: In = process.in
  lazy val out: Out = process.out
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(customValidator = Some(validator))
  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(variablesInit = Some(init))
  def withDefaultHeaders(headers: Map[String, String]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(defaultHeaders = headers)
  def withBodyOutputMapper(mapper: RequestOutput[ServiceOut] => Either[MappingError, Option[Out]]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(bodyOutputMapper = mapper)

  def defaultMock: Either[MockerError | MockedOutput, Option[Out]] =
    bodyOutputMapper(RequestOutput(process.defaultServiceMock, defaultHeaders)).left.map(
      err => MockerError(errorMsg = err.errorMsg)
    )

  def mapBodyOutput(
                               serviceOutput: ServiceOut,
                               headers: Seq[Seq[String]]
                             ) =
    bodyOutputMapper(
      RequestOutput(
        serviceOutput,
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      )
    )
end ServiceWorker


case class InValidator[In <: Product: CirceCodec](prototype: In, customValidator: Option[In => Either[ValidatorError, In]] = None):

  def variableNames: Seq[String] = prototype.productElementNames.toSeq

  def validate(inputParamsAsJson: Seq[Either[Any, (String, Option[Json])]]) =
    val jsonResult: Either[ValidatorError, Seq[(String, Option[Json])]] = inputParamsAsJson
      .partition(_.isRight) match
      case (successes, failures) if failures.isEmpty =>
        Right(
          successes.collect { case Right(value) => value }
        )
      case (_, failures) =>
        Left(
          ValidatorError(
            failures
              .collect { case Left(value) => value }
              .mkString("Validater Error(s):\n - ", " - ", "\n"),

          )
        )
    val json: Either[ValidatorError, JsonObject] = jsonResult
      .map(_.foldLeft(JsonObject()) { case (jsonObj, jsonKey -> jsonValue) =>
        println(s" - $jsonKey: ${jsonValue.getClass.getSimpleName} - $jsonValue")
        jsonObj.add(jsonKey, jsonValue.getOrElse(Json.Null))
      })
    json
      .flatMap (jsonObj =>
        decodeTo[In](jsonObj.asJson.toString)
          .left
          .map(ex =>
            ValidatorError(errorMsg = ex.errorMsg))
          .flatMap(in => customValidator.map(v => v(in)).getOrElse(Right(in)))
      )
  end validate

end InValidator

// ApiCreator that describes these variables
case class GeneralVariables(
                          servicesMocked: Boolean = false,
                          outputMockOpt: Option[Json] = None,
                          outputServiceMockOpt: Option[Json] = None,
                          mockedSubprocesses: Seq[String] = Seq.empty,
                          outputVariables: Seq[String] = Seq.empty,
                          handledErrors: Seq[String] = Seq.empty,
                          regexHandledErrors: Seq[String] = Seq.empty,
                          impersonateUserIdOpt: Option[String] = None,
                          serviceNameOpt: Option[String] = None
                        ):
  def isMocked(workerTopicName: String): Boolean =
     mockedSubprocesses.contains(workerTopicName)

end GeneralVariables

case class OutMocker[Out <: Product: Decoder](worker: Worker[?,Out, ?]):

  def mockOrProceed(
      generalVariables: GeneralVariables,
      jsonToCamunda: Json => Any
                   ): Either[MockerError | MockedOutput, Option[Out]] =
    ((generalVariables.servicesMocked, generalVariables.isMocked(worker.topic), generalVariables.outputMockOpt) match
      case (_, _, Some(outputMock)) => // if the outputMock is set than we mock
        decodeMock(outputMock, jsonToCamunda)
      case (_, true, _)
        if !isService => // if your process is NOT a Service check if it is mocked
        worker.defaultMock
      case (true, _, _)
        if isService => // if your process is a Service check if it is mocked
        worker.defaultMock
      case (_, _, None) =>
        Right(None)
      ).left.map(err => MockerError(err.errorMsg))

  end mockOrProceed

  private lazy val isService =  worker.isInstanceOf[ServiceWorker[?,?,?,?]]

  private def decodeMock[A <: Product : Decoder](
                                                    json: Json,
                                                    jsonToCamunda: Json => Any
                                                  ): Either[MockerError | MockedOutput, Option[A]] =
    (json.isObject, isService) match
      case (true, true) =>
        decodeTo(json.asJson.toString)
          .map(Some(_))
          .left
          .map(ex => MockerError(errorMsg = ex.errorMsg))
      case (true, _) =>
        Left(
          MockedOutput(mockedOutput =
            json.asObject.get.toMap
              .map { case k -> json =>
                k -> jsonToCamunda(json)
              }
          )
        )
      case _ =>
        Left(
          MockerError(errorMsg =
            s"The mock must be a Json Object:\n- $json\n- ${json.getClass}"
          )
        )
  end decodeMock

end OutMocker

case class RequestOutput[ServiceOut](
                                      outputBody: ServiceOut,
                                      headers: Map[String, String]
                                    )