package camundala
package worker

import camundala.bpmn.*
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.ValidatorError

case class Workers(workers: Seq[Worker[?]])

sealed trait Worker[In <: Product: CirceCodec]:
  def topic: String
  def in: In
  def inValidator: Option[InValidator[In]]

case class ProcessWorker[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec
](process: Process[In, Out],
  inValidator: Option[InValidator[In]] = None ) extends Worker[In]:
  lazy val topic: String = process.processName
  lazy val in: In = process.in
end ProcessWorker

case class ServiceProcessWorker[
  In <: Product: CirceCodec,
  Out <: Product: CirceCodec,
  ServiceIn <: Product: Encoder,
  ServiceOut : Decoder
](process: ServiceProcess[In, Out, ServiceIn, ServiceOut],
  inValidator: Option[InValidator[In]] = None) extends Worker[In]:
  lazy val topic: String = process.serviceName
  lazy val in: In = process.in

end ServiceProcessWorker


case class InValidator[In <: Product: CirceCodec](prototype: In, customValidator: In => Either[ValidatorError, In]):

  def inElementNames: Seq[String] = prototype.productElementNames.toSeq

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
        //  .flatMap(validate)

  end validate

end InValidator
