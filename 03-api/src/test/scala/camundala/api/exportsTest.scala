package camundala.api

class exportsTest extends munit.FunSuite:

  test("camundala.api.DefaultApiCreator UNDEFINED"):
    assertEquals(
      shortenTag("camundala.api.DefaultApiCreator"),
      "Camundala api Default Api Creator"
    )
  test("mycompany-myproject-myprocess.MyWorker"):
    assertEquals(
      shortenTag("mycompany-myproject-myprocess.MyWorker"),
      "Myprocess My Worker"
    )
  test("mycompany-myproject-myprocess.MyWorker.get"):
    assertEquals(
      shortenTag("mycompany-myproject-myprocess.MyWorker.get"),
      "My Worker get"
    )
  test("mycompany-myproject-myprocessV2-GetMyWorker"):
    assertEquals(
      shortenTag("mycompany-myproject-myprocessV2-MyWorker"),
      "My Worker"
    )
  test("mycompany-myproject-myprocessV4.MyWorker"):
    assertEquals(
      shortenTag("mycompany-myproject-myprocessV4.MyWorker"),
      "My Worker"
    )

end exportsTest
