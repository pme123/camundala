package camundala.bpmn

//sbt bpmn/testOnly *InOutDescrTest
class InOutDescrTest extends munit.FunSuite:

  test("shortName new Name"):
    assertEquals(
      InOutDescr("mycompany-myproject-myprocess.MyWorker").shortName,
      "MyWorker"
    )
  test("shortName old Name"):
    assertEquals(
      InOutDescr("mycompany-myproject-myWorker.post").shortName,
      "myWorker.post"
    )
end InOutDescrTest
