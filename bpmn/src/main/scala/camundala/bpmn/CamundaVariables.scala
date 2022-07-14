package camundala.bpmn

import io.circe.*
import io.circe.syntax.*

import scala.annotation.tailrec

sealed trait CamundaVariable:
  def value: Any

object CamundaVariable:

  implicit val encodeCamundaVariable: Encoder[CamundaVariable] =
    Encoder.instance {
      case v: CString => v.asJson
      case v: CInteger => v.asJson
      case v: CLong => v.asJson
      case v: CDouble => v.asJson
      case v: CBoolean => v.asJson
      case v: CFile => v.asJson
      case v: CJson => v.asJson
      case v: CEnum => v.asJson
      case CNull => Json.Null
    }

  given Schema[CamundaVariable] =
    Schema.derived

  given Schema[CString] = Schema.derived
  given Encoder[CString] = deriveEncoder
  given Decoder[CString] = deriveDecoder

  given Schema[CInteger] = Schema.derived
  given Encoder[CInteger] = deriveEncoder
  given Decoder[CInteger] = deriveDecoder

  given Schema[CLong] = Schema.derived
  given Encoder[CLong] = deriveEncoder
  given Decoder[CLong] = deriveDecoder

  given Schema[CDouble] = Schema.derived
  given Encoder[CDouble] = deriveEncoder
  given Decoder[CDouble] = deriveDecoder

  given Schema[CBoolean] = Schema.derived
  given Encoder[CBoolean] = deriveEncoder
  given Decoder[CBoolean] = deriveDecoder

  given Schema[CFile] = Schema.derived
  given Encoder[CFile] = deriveEncoder
  given Decoder[CFile] = deriveDecoder

  given Schema[CFileValueInfo] =
    Schema.derived
  given Encoder[CFileValueInfo] =
    deriveEncoder
  given Decoder[CFileValueInfo] =
    deriveDecoder

  given Schema[CJson] = Schema.derived
  given Encoder[CJson] = deriveEncoder
  given Decoder[CJson] = deriveDecoder

  given Schema[CEnum] = Schema.derived
  given Encoder[CEnum] = deriveEncoder
  given Decoder[CEnum] = deriveDecoder

  import reflect.Selectable.reflectiveSelectable

  def toCamunda[T <: Product: Encoder](
                                        product: T
                                      ): Map[String, CamundaVariable] =
    product.productElementNames
      .zip(product.productIterator)
      .filterNot { case k -> v => v.isInstanceOf[None.type] } // don't send null
      .map { case (k, v) => k -> objectToCamunda(product, k, v) }
      .toMap

  @tailrec
  def objectToCamunda[T <: Product: Encoder](
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
      case v: (Product | Iterable[?] | Map[?, ?])
        if !v.isInstanceOf[scala.reflect.Enum] =>
        CJson(
          product.asJson.deepDropNullValues.hcursor
            .downField(key)
            .as[Json] match
            case Right(v) => v.toString
            case Left(ex) =>
              throwErr(s"$key of $v could NOT be Parsed to a JSON!\n$ex")
        )
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
        CEnum(v.toString)
      case other if other == null =>
        CNull
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

  case class CEnum(value: String, private val `type`: String = "String")
    extends CamundaVariable

  case class CJson(value: String, private val `type`: String = "Json")
    extends CamundaVariable

  implicit val decodeCamundaVariable: Decoder[CamundaVariable] =
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

end CamundaVariable

