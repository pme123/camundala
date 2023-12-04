package camundala.domain


import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromString, writeToString}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

// Jsoniter
type InOutCodec[T] = JsonValueCodec[T]

inline def deriveInOutCodec[A]: InOutCodec[A] = JsonCodecMaker.make {
  CodecMakerConfig
    .withTransientNone(true)
    .withTransientEmpty(true)
    .withTransientDefault(false)
    .withRequireDefaultFields(true)
    .withRequireDiscriminatorFirst(false)
  //.withDiscriminatorFieldName(None)
  //.withCirceLikeObjectEncoding(true)
}

inline def deriveEnumInOutCodec[A]: InOutCodec[A] = JsonCodecMaker.make {
  CodecMakerConfig
    .withTransientDefault(false)
    .withRequireDefaultFields(true)
    .withDiscriminatorFieldName(None)
}

extension[T](obj: T)
  def toJsonStr(using JsonValueCodec[T]): String =
    writeToString(obj)

end extension
extension(jsonStr: String)
  def toObject[T](using JsonValueCodec[T]): T =
    readFromString[T](jsonStr)

end extension
