package camundala.domain

class DomainTest extends munit.FunSuite:

  test("niceName"):
    assertEquals(
      "the-coolDaddy".niceName,
      "The Cool Daddy"
    )
  test("shortName of Generic Service simple"):
    assertEquals(
      GenericServiceIn.shortServiceName("loadClient-post"),
      "loadClient-post"
    )

  test("shortName of Generic Service"):
    assertEquals(
      GenericServiceIn.shortServiceName("service-loader.post"),
      "loader.post"
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
