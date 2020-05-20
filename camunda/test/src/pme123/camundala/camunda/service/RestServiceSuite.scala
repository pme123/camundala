package pme123.camundala.camunda.service

import eu.timepit.refined.auto._
import pme123.camundala.camunda.CamundaLayers
import pme123.camundala.camunda.service.restService.QueryParams.Params
import pme123.camundala.camunda.service.restService.Request.Auth.{BasicAuth, NoAuth}
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.camunda.service.restService.Response.NoContent
import pme123.camundala.camunda.service.restService.{Request, RequestMethod}
import pme123.camundala.model.bpmn.PropKey
import pme123.camundala.model.deploy.{Sensitive, Url}
import sttp.client._
import zio.UIO
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
        }
      ),
      suite("Call Service")(
        testM("SWAPI with get method") {
          for {
            result <- restService.call(Request(
              Host(url, NoAuth),
              path = Path("people", "1")))
          } yield
            assert(result)(isSubtype[restService.Response.WithContent](anything))
        }
      ).provideCustomLayer(CamundaLayers.restServicetLayer).mapError(TestFailure.fail)
    )

  val key1: PropKey = "name"
  val key2: PropKey = "iq"
  private val queryParams = Params(key1 -> "Mäder", key2 -> "99")

  private val url: Url = "https://swapi.dev/api"
  private val host = Host(url,
    BasicAuth("pme123", Sensitive("pwd123x")))
}