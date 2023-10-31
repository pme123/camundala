package camundala
package camunda7.worker

import domain.*
import bpmn.*
import camundala.worker.EngineContext
import io.circe.*
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.variable.impl.value.JsonValueImpl
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}

import java.net.URLDecoder
import java.nio.charset.Charset
import java.time.LocalDateTime

export sttp.model.{Method, Uri, QueryParams}
export org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
export org.springframework.context.annotation.Configuration

lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

//TODO REMOVE
def toCamunda[T <: Product: Encoder](
    product: T
): Map[String, Any] =
  product.productElementNames
    .zip(product.productIterator)
    // .filterNot { case _ -> v => v.isInstanceOf[None.type] } // don't send null
    .map { case (k, v) => k -> objectToCamunda(product, k, v) }
    .toMap

def toCamunda(
    variables: Map[String, Json]
): Map[String, Any] =
  variables
    .map { case (k, v) => k -> jsonToCamunda(v) }

def objectToCamunda[T <: Product: Encoder](
    product: T,
    key: String,
    value: Any
): Any =
  value match
    case None | null => null
    case Some(v)     => objectToCamunda(product, key, v)
    case v: (Product | Iterable[?] | Map[?, ?]) =>
      product.asJson.deepDropNullValues.hcursor
        .downField(key)
        .as[Json] match {
        case Right(v) => jsonToCamunda(v)
        case Left(ex) =>
          throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
      }

    case v =>
      valueToCamunda(v)

def valueToCamunda(value: Any): Any =
  value match
    case v: scala.reflect.Enum =>
      v.toString
    case ldt: LocalDateTime =>
      ldt.toString
    case other if other == null =>
      null
    case v: Json =>
      jsonToCamunda(v)
    case other =>
      other

def domainObjToCamunda[A <: Product: CirceCodec](variable: A): Any =
  new JsonValueImpl(variable.asJson.toString)

def jsonToCamunda(json: Json): Any =
  json match
    case j if j.isNull    => null
    case j if j.isNumber  => j.asNumber.get.toBigDecimal.get
    case j if j.isBoolean => j.asBoolean.get
    case j if j.isString  => j.asString.get
    case j =>
      new JsonValueImpl(j.toString)

end jsonToCamunda

type HelperContext[T] = ExternalTask ?=> T
// end REMOVE
