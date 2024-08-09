package camundala.bpmn

class exportsTest extends munit.FunSuite:

  test("mycompany-myproject-myprocessV1.MyWorker NEW"):
    assertEquals(
      shortenName("mycompany-myprocessV1-myprocess.MyWorker"),
      "myprocess.MyWorker"
    )

  test("mycompany-myproject-myprocessV1.MyWorker.get OLD"):
    assertEquals(
      shortenName("mycompany-myproject-myprocessV1.MyWorker.get"),
      "MyWorker.get"
    )
  test("mycompany-myproject-myprocessV1-GetMyWorker NEW"):
    assertEquals(
      shortenName("mycompany-myproject-myprocessV1-GetMyWorker"),
      "GetMyWorker"
    )
  test("mycompany-myproject-myprocess.MyWorker OTHER"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess.MyWorker"),
      "myproject-myprocess.MyWorker"
    )

  test("mycompany-myproject-myprocess.MyWorker.get OLD"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess.MyWorker.get"),
      "MyWorker.get"
    )
  test("mycompany-myproject-myprocess-GetMyWorker OTHER"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess-GetMyWorker"),
      "myprocess-GetMyWorker"
    )

  test("mycompany-myproject.myprocess.GetMyWorker REST"):
    assertEquals(
      shortenName("mycompany-myproject.myprocess.GetMyWorker"),
      "mycompany-myproject.myprocess.GetMyWorker"
    )




end exportsTest