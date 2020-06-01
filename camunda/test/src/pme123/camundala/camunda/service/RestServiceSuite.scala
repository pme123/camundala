package pme123.camundala.camunda.service

import eu.timepit.refined.auto._
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.camunda.TestData._
import pme123.camundala.camunda.service.restService.QueryParams.Params
import pme123.camundala.camunda.service.restService.Request.Host
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
          assert(result)(equalTo(uri"$url/hello?name=Mäder&iq=99"))
        },
        test("without path and query params") {
          val result = restService.uri(Request(host,
            queryParams = queryParams))
          assert(result)(equalTo(uri"$url?name=Mäder&iq=99"))
        },
        test("withpath and query params and mapping") {
          val result = restService.uri(Request(host,
            path = Path("hello", "__variable"),
            queryParams = Params(key1 -> "Mäder", key2 -> "__value"),
            mappings = Map("__variable" -> "Kurt", "__value" -> "101")))
          assert(result)(equalTo(uri"$url/hello/Kurt?name=Mäder&iq=101"))
        }
      ),
      suite("Call Service")(
        testM("SWAPI with get method") {
          for {
            result <- restService.call(testRequest)
          } yield
            assert(result)(isSubtype[restService.Response.WithContent](anything))
        }
      ).provideCustomLayer(CamundaLayers.restServicetLayer).mapError(TestFailure.fail)
    )

  val key1: PropKey = "name"
  val key2: PropKey = "iq"
  val queryParams: Params = Params(key1 -> "Mäder", key2 -> "99")
}
