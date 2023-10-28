package camundala
package worker

import camundala.worker.CamundalaWorkerError.ServiceError
import domain.*

import java.time.LocalDateTime
import scala.util.Either

trait EngineContext :
  protected def toEngineObject: Json => Any
  def generalVariables: GeneralVariables

  def toEngineObject[T <: Product : Encoder](
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
      .map { case (k, v) => k -> jsonToEngineObject(v) }

  def objectToEngineObject[T <: Product : Encoder](
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
          .as[Json] match {
          case Right(v) => jsonToEngineObject(v)
          case Left(ex) =>
            throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
        }

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
        jsonToEngineObject(v)
      case other =>
        other

  def domainObjToEngineObject[A <: Product : CirceCodec](variable: A): Any =
    toEngineObject(variable.asJson)

  private def jsonToEngineObject(json: Json): Any =
    json match
      case j if j.isNull => null
      case j if j.isNumber => j.asNumber.get.toBigDecimal.get
      case j if j.isBoolean => j.asBoolean.get
      case j if j.isString => j.asString.get
      case j =>
        toEngineObject(j)

  end jsonToEngineObject

end EngineContext

