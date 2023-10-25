package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.{InitializerError, ValidatorError}

import scala.reflect.ClassTag

case class Workers(workers: Seq[Worker[?,?]])

sealed trait Worker[In <: Product: CirceCodec : ClassTag, T <: Worker[In, ?]]:
  def topic: String
  def in: In
  def inValidator: InValidator[In]


  protected def variablesInit: Option[In => Either[InitializerError, Map[String, Any]]]
  def withCustomValidator(customValidator: In => Either[ValidatorError, In]): T
  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): T


  //TODO move functions to WorkerHandler
  def executeWorker(processVariables: Seq[Either[ValidatorError, (String, Option[Json])]]) =
    for {
      validatedInput <- inValidator.validate(processVariables)
      initializedInput <- initVariables(validatedInput)

    } yield initializedInput

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
                         ) extends Worker[In, ProcessWorker[In, Out]]:
  lazy val topic: String = process.processName
  lazy val in: In = process.in
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ProcessWorker[In, Out] =
    copy(customValidator = Some(validator))

  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ProcessWorker[In, Out] =
      copy(variablesInit = Some(init))

end ProcessWorker

case class ServiceWorker[
  In <: Product: CirceCodec : ClassTag,
  Out <: Product: CirceCodec,
  ServiceIn <: Product: Encoder,
  ServiceOut : Decoder
](process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
  customValidator: Option[In => Either[ValidatorError, In]] = None,
  variablesInit: Option[In => Either[InitializerError, Map[String, Any]]] = None,
                         ) extends Worker[In, ServiceWorker[In,Out,ServiceIn, ServiceOut]]:
  lazy val topic: String = process.serviceName
  lazy val in: In = process.in
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(customValidator = Some(validator))

  def withInitVariables(init: In => Either[InitializerError, Map[String, Any]]): ServiceWorker[In, Out, ServiceIn, ServiceOut] =
    copy(variablesInit = Some(init))
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
      .flatMap: jsonObj =>
        decodeTo[In](jsonObj.asJson.toString)
          .left
          .map(ex =>
            ValidatorError(errorMsg = ex.errorMsg))
          .flatMap(in => customValidator.map(v => v(in)).getOrElse(Right(in)))

  end validate

end InValidator


case class VariablesInit[In <: Product: CirceCodec](init: In => Either[InitializerError, Map[String, Any]])
