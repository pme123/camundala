package camundala.bpmn

import scala.reflect.Enum

class FielNameTest extends munit.FunSuite:

  case class CaseClass(name: String = "peter", id: Int = 12)

  enum CaseEnum:
    case A(name: String = "peter")
    case B(id: Int = 12)

  test("productElementNames Case Class"):
    assertEquals(
      allFieldNames[CaseClass],
      Seq("name", "id")
    )

  test("productElementNames Enum"):
    assertEquals(
      allFieldNames[CaseEnum],
      Seq("name", "id")
    )
end FielNameTest
