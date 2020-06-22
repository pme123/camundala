package pme123.camundala.services

import java.io.{File, PrintWriter}

import eu.timepit.refined.auto._
import pme123.camundala.camunda.DeployRequest
import pme123.camundala.model.bpmn.Url
import sttp.client._
import sttp.model.Uri
import zio.test.Assertion._
import zio.test.TestAspect.ignore
import zio.test._

object HttpServerSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8888"
  println(s"Camundala must run on $url!")
  implicit val backend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("HttpServerSuite")(
      test("get Index") {
        assert(get(uri"$url"))(containsString("<title>Camundala API</title>"))
      },
      test("get Depoyment") {
        assert(get(uri"$url/deployment"))(equalTo("Services are up and running"))
      },
      test("get Depoyment with Deploy Id") {
        assert(get(uri"$url/myId/deployment"))(equalTo(s"Services are up and running for DeployId: myId"))

      },
      test("post Deploy Multpart") {
        val testFile = File.createTempFile("user-123", ".bpmn")
        val pw = new PrintWriter(testFile)
        pw.write("""<?xml version="1.0" encoding="UTF-8"?>
                   |<definitions></definitions>""".stripMargin)
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
      } @@ ignore // needs work to make reliable test
    )

  private def get(uri: Uri) = {
    basicRequest
      .response(asStringAlways)
      .get(uri)
      .send()
      .body
  }
}
