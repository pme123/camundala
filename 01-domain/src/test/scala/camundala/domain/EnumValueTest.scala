package camundala.domain

import io.circe.*
import io.circe.Decoder.Result

class EnumValueTest extends munit.FunSuite:

  inline def deriveEnumValueEncoder[A]: Encoder[A] =
    Encoder.instance: a =>
      Json.fromString(a.toString)

  inline def deriveEnumValueDecoder[A]: Decoder[A] =
    Decoder.decodeString.emap: str =>
      if str == valueOf[A].toString
      then Right(valueOf[A])
      else Left(s"Invalid value: $str - expected: ${valueOf[A]}")

  inline def deriveEnumValueInOutCodec[A]: InOutCodec[A] =
    CirceCodec.from(deriveEnumValueDecoder[A], deriveEnumValueEncoder[A])

  test("CmsTest encode and decode enum type"):
    val obj  = MyClass(MyEnum.Y)
    val json = obj.asJson
    assertEquals(Json.obj("processStatus" -> Json.fromString("Y")), json)
    assertEquals(Right(obj), json.as[MyClass])

  test("bad enum value"):
    val json = Json.obj("processStatus" -> Json.fromString("Z"))
    assertEquals(Left("Invalid value: Z - expected: Y"), json.as[MyClass].left.map(_.message))

  case class MyClass(processStatus: MyEnum.Y.type)
  object MyClass:
    given InOutCodec[MyClass]       = deriveInOutCodec
    given InOutCodec[MyEnum.Y.type] = deriveEnumValueInOutCodec

  enum MyEnum:
    case X, Y
  object MyEnum:
    given InOutCodec[MyEnum] = deriveEnumInOutCodec

end EnumValueTest
