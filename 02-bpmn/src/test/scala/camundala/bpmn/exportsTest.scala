package camundala.bpmn

class exportsTest extends munit.FunSuite:

  test("mycompany-myproject-myprocessV1-GetMyWorker NEW"):
    assertEquals(
      shortenName("mycompany-myproject-myprocessV1-GetMyWorker"),
      "GetMyWorker"
    )

  test("mycompany-myproject-myprocessV1.MyWorker.get OLD1"):
    assertEquals(
      shortenName("mycompany-myproject-myprocessV1.MyWorker.get"),
      "MyWorker.get"
    )
  test("mycompany-myproject-myprocess.MyWorker.delete OLD1"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess.MyWorker.delete"),
      "MyWorker.delete"
    )

  test("mycompany-myproject-myprocessV1.MyWorker OLD2"):
    assertEquals(
      shortenName("mycompany-myproject-myprocessV1.MyWorker"),
      "MyWorker"
    )

  test("mycompany-myproject-myprocess-other-MyWorker OLD31"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess-other-MyWorker"),
      "myprocess-other-MyWorker"
    )

  test("mycompany-myproject-myprocess-GetMyWorker OLD32"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess-GetMyWorker"),
      "myprocess-GetMyWorker"
    )

  test("mycompany-myproject-myprocess.MyWorker OLD4"):
    assertEquals(
      shortenName("mycompany-myproject-myprocess.MyWorker"),
      "myprocess.MyWorker"
    )

  test("mycompany-myproject-GetMyWorker OLD4"):
    assertEquals(
      shortenName("mycompany-myproject-GetMyWorker"),
      "GetMyWorker"
    )

  test("mycompany-myproject.myprocess.GetMyWorker REST"):
    assertEquals(
      shortenName("mycompany-myproject.myprocess.GetMyWorker"),
      "mycompany-myproject.myprocess.GetMyWorker"
    )

end exportsTest
