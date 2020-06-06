package pme123.camundala.services

import java.io.PrintWriter

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.Url
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, ZSpec, assert, environment, suite, test}
import sttp.client._
import java.io.File
import sttp.tapir.openapi.OpenAPI
import sttp.tapir.docs.openapi._
import pme123.camundala.camunda.DeployRequest
import sttp.model.Uri

object HttpServerSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8888"
  println(s"Camundala must run on $url!")
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("HttpServerSuite")(
      test("get Index") {
        getAndAssert(uri"$url")
      },
      test("get Depoyment") {
        getAndAssert(uri"$url/deployment")
      },
      test("get Depoyment with Deploy Id") {
        getAndAssert(uri"$url/myId/deployment", Some("myId"))

      },
      test("post Deploy Multpart") {
        val testFile = File.createTempFile("user-123", ".bpmn")
        val pw = new PrintWriter(testFile)
        pw.write("This is not a photo")
        pw.close()
        import DeployRequest._
        // testing
        val result: String = basicRequest
          .response(asStringAlways)
          .post(uri"$url/deployment/create")
          .multipartBody(multipart(DeploymentName, "Frodo"),
            multipart(EnableDuplicateFiltering, "true"),
            multipart(DeployChangedOnly, "true"),
            multipartFile("testFile", testFile))
          .send()
          .body
        println("Got result: " + result)

        assert(result)(equalTo("Services are up and running"))
      }
    )

  private def getAndAssert(uri: Uri, deployId: Option[String] = None) = {
    val result: String = basicRequest
      .response(asStringAlways)
      .get(uri)
      .send()
      .body
    assert(result)(equalTo("Services are up and running" + deployId.map(di => s" for DeployId: $di").getOrElse("")))
  }
}
