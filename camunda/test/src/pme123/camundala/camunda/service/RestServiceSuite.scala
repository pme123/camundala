package pme123.camundala.camunda.service

import eu.timepit.refined.auto._
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.camunda.TestData._
import pme123.camundala.camunda.service.restService.QueryParams.Params
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.camunda.service.restService.{Request, RequestMethod}
import pme123.camundala.model.bpmn.PropKey
import sttp.client._
import zio.test.Assertion._
import zio.test._

object RestServiceSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("RestServiceSuite")(
      suite("Create URI")(
        test("with path") {
          val result = restService.uri(Request(host,
            RequestMethod.Get,
            Path("hello")))
          assert(result)(equalTo(uri"$url/hello"))
        },
        test("without path") {
          val result = restService.uri(Request(host))
          assert(result)(equalTo(uri"$url"))
        },
        test("with path and query params") {
          val result = restService.uri(Request(host,
            RequestMethod.Get,
            Path("hello"),
            queryParams))
          assert(result)(equalTo(uri"$url/hello?name=Mäder & Söhne&iq=99"))
        },
        test("without path and query params") {
          val result = restService.uri(Request(host,
            queryParams = queryParams))
          assert(result)(equalTo(uri"$url?name=Mäder & Söhne&iq=99"))
        },
        test("with path and query params and mapping") {
          val result = restService.uri(Request(host,
            path = Path("hello", "%variable"),
            queryParams = Params(key1 -> "Mäder", key2 -> "%value", key3 -> "%other"),
            ), Map("variable" -> Some("Kurt"), "value" -> Some("101"), "other" -> None))
          assert(result)(equalTo(uri"$url/hello/Kurt?name=Mäder&iq=101"))
        },
        test("with body and mapping") {
          val result = restService.mapStr(
            """{ "name": %object,
              |"type": "%strType"
              |}""".stripMargin,
            Map("object" ->
              Some("""{
                |"sub": "great"
                |}""".stripMargin), "strType" -> Some("bool")))
          assert(result)(equalTo(
            """{ "name": {
              |"sub": "great"
              |},
              |"type": "bool"
              |}""".stripMargin))
        }
      ),
      suite("Call Service")(
        testM("SWAPI with get method") {
          for {
            result <- restService.call(testRequest, Map.empty)
          } yield
            assert(result)(isSubtype[restService.Response.WithContent](anything))
        }
      ).provideCustomLayer(CamundaLayers.restServicetLayer).mapError(TestFailure.fail)
    )

  val key1: PropKey = "name"
  val key2: PropKey = "iq"
  val key3: PropKey = "other"
  val queryParams: Params = Params(key1 -> "Mäder & Söhne", key2 -> "99")
}
