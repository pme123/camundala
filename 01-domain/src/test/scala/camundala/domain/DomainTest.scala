package camundala.domain

class DomainTest extends munit.FunSuite:

  test("niceName"):
    assertEquals(
      "the-coolDaddy".niceName,
      "The Cool Daddy"
    )

end DomainTest
