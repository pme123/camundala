package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.ServiceError

import java.time.LocalDateTime

trait EngineContext:
  def getLogger(clazz: Class[?]): WorkerLogger
  def toEngineObject: Json => Any

  def sendRequest[ServiceIn <: Product: Encoder, ServiceOut: Decoder](
      request: RunnableRequest[ServiceIn]
  ): Either[ServiceError, ServiceResponse[ServiceOut]]
end EngineContext

trait WorkerLogger:
  def debug(message: String): Unit
  def info(message: String): Unit
  def warn(message: String): Unit
  def error(err: CamundalaWorkerError): Unit
end WorkerLogger

final case class EngineRunContext(engineContext: EngineContext, generalVariables: GeneralVariables):

  def getLogger(clazz: Class[?]): WorkerLogger = engineContext.getLogger(clazz)

  def sendRequest[ServiceIn <: Product: Encoder, ServiceOut: Decoder](
      request: RunnableRequest[ServiceIn]
  ): Either[ServiceError, ServiceResponse[ServiceOut]] =
    engineContext.sendRequest(request)

  def jsonObjectToEngineObject(
      json: JsonObject
  ): Map[String, Any] =
    json.toMap
      .map { case (k, v) => k -> jsonToEngineValue(v) }

  def toEngineObject[T <: Product: Encoder](
      product: T
  ): Map[String, Any] =
    product.productElementNames
      .zip(product.productIterator)
      // .filterNot { case _ -> v => v.isInstanceOf[None.type] } // don't send null
      .map { case (k, v) => k -> objectToEngineObject(product, k, v) }
      .toMap

  def toEngineObject(
      variables: Map[String, Json]
  ): Map[String, Any] =
    variables
      .map { case (k, v) => k -> jsonToEngineValue(v) }

  def objectToEngineObject[T <: Product: Encoder](
      product: T,
      key: String,
      value: Any
  ): Any =
    value match
      case None | null => null
      case Some(v) => objectToEngineObject(product, key, v)
      case v: (Product | Iterable[?] | Map[?, ?]) =>
        product.asJson.deepDropNullValues.hcursor
          .downField(key)
          .as[Json] match
          case Right(v) => jsonToEngineValue(v)
          case Left(ex) =>
            throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")

      case v =>
        valueToEngineObject(v)

  def valueToEngineObject(value: Any): Any =
    value match
      case v: scala.reflect.Enum =>
        v.toString
      case ldt: LocalDateTime =>
        ldt.toString
      case other if other == null =>
        null
      case v: Json =>
        jsonToEngineValue(v)
      case other =>
        other

  def domainObjToEngineObject[A <: Product: JsonCodec](variable: A): Any =
    toEngineObject(variable.asJson)

  def jsonToEngineValue(json: Json): Any =
    json match
      case j if j.isNull => null
      case j if j.isNumber =>
        j.asNumber.get.toBigDecimal.get match
          case n if n.isValidInt => n.toInt
          case n if n.isValidLong => n.toLong
          case n => n.toDouble
      case j if j.isBoolean => j.asBoolean.get
      case j if j.isString => j.asString.get
      case j =>
        toEngineObject(j)
  end jsonToEngineValue

  private def toEngineObject: Json => Any = engineContext.toEngineObject

end EngineRunContext
