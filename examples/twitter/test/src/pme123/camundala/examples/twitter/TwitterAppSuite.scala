package pme123.camundala.examples.twitter

import eu.timepit.refined.auto._
import pme123.camundala.app.appRunner
import pme123.camundala.model.bpmn.StaticFile
import pme123.camundala.model.bpmn.Url
import pme123.camundala.services.{ServicesLayers, StandardApp}
import zio.test.Assertion._
import zio.test._
import zio.test.TestAspect.ignore
object TwitterAppSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8080/camunda"

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("TwitterAppSuite")(
      testM("compile bpmnModels") {
        for {
          result <- appRunner.update()
        } yield
          assert(result)(isUnit)
      }@@ ignore // see https://stackoverflow.com/questions/62131981/scala-script-engine-is-not-found-when-run-in-test-using-mill
    ).provideCustomLayer(ServicesLayers.appDepsLayer >>>
      StandardApp.layer(classOf[TwitterApp], StaticFile("bpmnModels.sc", ".")))
        .mapError(TestFailure.fail)

}
