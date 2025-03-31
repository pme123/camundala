package camundala.domain

import camundala.domain.*
import munit.FunSuite

class InOutTest extends FunSuite:

  test("inVariableNames should return distinct variable names"):
    val process               =
      Process(InOutDescr("test-id", TestInOut.A(), NoOutput()), NoInput(), ProcessLabels.none)
        .withEnumInExample(TestInOut.B())
    val expectedVariableNames = Seq("name", "isSad", "isFunny")
    assertEquals(process.inVariableNames, expectedVariableNames)

  test("outVariableNames should return distinct variable names"):
    val process               =
      Process(InOutDescr("test-id", NoInput(), TestInOut.A()), NoInput(), ProcessLabels.none)
        .withEnumOutExample(TestInOut.B())
    val expectedVariableNames = Seq("name", "isSad", "isFunny")
    assertEquals(process.outVariableNames, expectedVariableNames)

  test("inVariables should return distinct variables"):
    val process           =
      Process(InOutDescr("test-id", TestInOut.A(), NoOutput()), NoInput(), ProcessLabels.none)
        .withEnumInExample(TestInOut.B())
    val expectedVariables = Seq("name" -> "hello", "isSad" -> true, "isFunny" -> false)
    assertEquals(process.inVariables, expectedVariables)

  test("outVariables should return distinct variables"):
    val process           =
      Process(InOutDescr("test-id", NoInput(), TestInOut.A()), NoInput(), ProcessLabels.none)
        .withEnumOutExample(TestInOut.B())
    val expectedVariables = Seq("name" -> "hello", "isSad" -> true, "isFunny" -> false)
    assertEquals(process.outVariables, expectedVariables)

end InOutTest

enum TestInOut:
  case A(name: String = "hello", isSad: Boolean = true)
  case B(name: String = "world", isFunny: Boolean = false)

object TestInOut:
  given ApiSchema[TestInOut]  = deriveApiSchema
  given InOutCodec[TestInOut] = deriveInOutCodec
