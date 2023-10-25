package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.ValidatorError

case class Workers(workers: Seq[Worker[?,?]])

sealed trait Worker[In <: Product: CirceCodec, T <: Worker[In, ?]]:
  def topic: String
  def in: In
  def inValidator: InValidator[In]
  def withCustomValidator(customValidator: In => Either[ValidatorError, In]): T
end Worker

case class ProcessWorker[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec
](process: Process[In, Out],
  customValidator: Option[In => Either[ValidatorError, In]] = None
                         ) extends Worker[In, ProcessWorker[In, Out]]:
  lazy val topic: String = process.processName
  lazy val in: In = process.in
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ProcessWorker[In, Out] =
    copy(customValidator = Some(validator))

end ProcessWorker

case class ServiceWorker[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec,
  ServiceIn <: Product: Encoder,
  ServiceOut : Decoder
](process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
  customValidator: Option[In => Either[ValidatorError, In]] = None
                                ) extends Worker[In, ServiceWorker[In,Out,ServiceIn, ServiceOut]]:
  lazy val topic: String = process.serviceName
  lazy val in: In = process.in
  def inValidator: InValidator[In] = InValidator(process.in, customValidator)

  def withCustomValidator(validator: In => Either[ValidatorError, In]): ServiceWorker[In,Out,ServiceIn, ServiceOut] =
    copy(customValidator = Some(validator))
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
