package camundala
package bpmn

import camundala.domain.*
import io.circe.*
import io.circe.Json.*

import java.time.LocalDateTime
import scala.annotation.tailrec

sealed trait CamundaVariable:
  def value: Any

object CamundaVariable:

  given Encoder[CamundaVariable] =
    Encoder.instance {
      case v: CString => v.asJson
      case v: CInteger => v.asJson
      case v: CLong => v.asJson
      case v: CDouble => v.asJson
      case v: CBoolean => v.asJson
      case v: CFile => v.asJson
      case v: CJson => v.asJson
      case CNull => Json.Null
    }

  given ApiSchema[CamundaVariable] =
    deriveApiSchema

  given ApiSchema[CString] = deriveApiSchema
  given InOutCodec[CString] = deriveInOutCodec

  given ApiSchema[CInteger] = deriveApiSchema
  given InOutCodec[CInteger] = deriveInOutCodec

  given ApiSchema[CLong] = deriveApiSchema
  given InOutCodec[CLong] = deriveInOutCodec

  given ApiSchema[CDouble] = deriveApiSchema
  given InOutCodec[CDouble] = deriveInOutCodec

  given ApiSchema[CBoolean] = deriveApiSchema
  given InOutCodec[CBoolean] = deriveInOutCodec

  given ApiSchema[CFile] = deriveApiSchema
  given InOutCodec[CFile] = deriveInOutCodec

  given ApiSchema[CFileValueInfo] = deriveApiSchema
  given InOutCodec[CFileValueInfo] = deriveInOutCodec

  given ApiSchema[CJson] = deriveApiSchema
  given InOutCodec[CJson] = deriveInOutCodec

  def toCamunda[T <: Product: InOutEncoder](
      products: Seq[T]
  ): Seq[Map[String, CamundaVariable]] =
    products.map(toCamunda)

  def toCamunda[T <: Product: InOutEncoder](
      product: T
  ): Map[String, CamundaVariable] =
    product.productElementNames
      .zip(product.productIterator)
     // .filterNot { case _ -> v => v.isInstanceOf[None.type] } // don't send null
      .map { case (k, v) => k -> objectToCamunda(product, k, v) }
      .toMap

  @tailrec
  def objectToCamunda[T <: Product: InOutEncoder](
      product: T,
      key: String,
      value: Any
  ): CamundaVariable =
    value match
      case None => CNull
      case Some(v) => objectToCamunda(product, key, v)
      case f @ FileInOut(fileName, _, mimeType) =>
        CFile(
          f.contentAsBase64,
          CFileValueInfo(
            fileName,
            mimeType
          )
        )
      case v: (Product | Iterable[?] | Map[?, ?]) =>
        product.asJson.deepDropNullValues.hcursor
          .downField(key)
          .as[Json] match {
            case Right(str) if str.isString => CString(str.asString.get) // Pure Enum!
            case Right(v) => CJson(v.toString)
            case Left(ex) => throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
          }

      case v =>
        valueToCamunda(v)

  def valueToCamunda(value: Any): CamundaVariable =
    value match
      case v: String =>
        CString(v)
      case v: Int =>
        CInteger(v)
      case v: Long =>
        CLong(v)
      case v: Boolean =>
        CBoolean(v)
      case v: Float =>
        CDouble(v.toDouble)
      case v: Double =>
        CDouble(v)
      case v: scala.reflect.Enum =>
        CString(v.toString)
      case ldt: LocalDateTime =>
        CString(ldt.toString)
      case other if other == null =>
        CNull
      case v: Json =>
        CJson(v.toString)
      case other =>
        throwErr(s"Unexpected Value to map to CamundaVariable: $other")

  case object CNull extends CamundaVariable:
    val value: Null = null

    private val `type`: String = "String"
  case class CString(value: String, private val `type`: String = "String")
      extends CamundaVariable
  case class CInteger(value: Int, private val `type`: String = "Integer")
      extends CamundaVariable
  case class CLong(value: Long, private val `type`: String = "Long")
      extends CamundaVariable
  case class CBoolean(value: Boolean, private val `type`: String = "Boolean")
      extends CamundaVariable

  case class CDouble(value: Double, private val `type`: String = "Double")
      extends CamundaVariable

  case class CFile(
      @description("The File's content as Base64 encoded String.")
      value: String,
      valueInfo: CFileValueInfo,
      private val `type`: String = "File"
  ) extends CamundaVariable

  case class CFileValueInfo(
      filename: String,
      mimetype: Option[String]
  )

  case class CJson(value: String, private val `type`: String = "Json")
      extends CamundaVariable

  given Decoder[CamundaVariable] =
    (c: HCursor) =>
      for
        valueType <- c.downField("type").as[String]
        anyValue = c.downField("value")
        value <- decodeValue(valueType, anyValue, c.downField("valueInfo"))
      yield value

  def decodeValue(
      valueType: String,
      anyValue: ACursor,
      valueInfo: ACursor
  ): Either[DecodingFailure, CamundaVariable] =
    valueType match
      case "Null" => Right(CNull)
      case "Boolean" => anyValue.as[Boolean].map(CBoolean(_))
      case "Integer" => anyValue.as[Int].map(CInteger(_))
      case "Long" => anyValue.as[Long].map(CLong(_))
      case "Double" => anyValue.as[Double].map(CDouble(_))
      case "Json" => anyValue.as[String].map(CJson(_))
      case "File" =>
        valueInfo.as[CFileValueInfo].map(vi => CFile("not_set", vi))
      case _ => anyValue.as[String].map(CString(_))

  type JsonToCamundaValue = CamundaVariable | Map[String, CamundaVariable] |
    Seq[Any]

  def jsonToCamundaValue(json: Json): JsonToCamundaValue =

    val folder: Json.Folder[JsonToCamundaValue] =
      new Json.Folder[JsonToCamundaValue] {
        def onNull: CamundaVariable = CNull

        def onBoolean(value: Boolean): CamundaVariable = CBoolean(value)

        def onNumber(value: JsonNumber): CamundaVariable =
          value.toBigDecimal
            .map {
              case v if v.isValidInt => CInteger(v.intValue)
              case v if v.isValidLong => CLong(v.longValue)
              case v => CDouble(v.doubleValue)
            }
            .getOrElse(CDouble(value.toDouble))

        def onString(value: String): CamundaVariable = CString(value)

        def onArray(value: Vector[Json]): JsonToCamundaValue =
          value.collect {
            case v if !v.isNull => v.foldWith(this)
          }

        def onObject(value: JsonObject): JsonToCamundaValue =
          value
            .filter { case (_, v) => !v.isNull }
            .toMap
            .map((k, v) =>
              k -> (jsonToCamundaValue(v) match
                case cv: CamundaVariable => cv
                case _: (Map[?, ?] | Seq[?]) => CJson(v.toString)
              )
            )
      }
    json.foldWith(folder)

  end jsonToCamundaValue

end CamundaVariable
