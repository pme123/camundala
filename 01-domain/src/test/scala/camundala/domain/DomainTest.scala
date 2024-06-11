package camundala.domain

class DomainTest extends munit.FunSuite:

  test("niceName"):
    assertEquals(
      "the-coolDaddy".niceName,
      "The Cool Daddy"
    )

  test("enum derivation"):
    enum EE:
      case A, B
    object EE:
      given InOutCodec[EE] = deriveEnumInOutCodec

    assertEquals(
      EE.A.asJson,
      Json.fromString("A")
    )
    assertEquals(
      Json.fromString("B").as[EE].toOption.get,
      EE.B
    )
end DomainTest
