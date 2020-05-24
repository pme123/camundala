package pme123.camundala.examples.twitter

import eu.timepit.refined.auto._
import pme123.camundala.app.appRunner
import pme123.camundala.model.bpmn.StaticFile
import pme123.camundala.model.deploy.Url
import pme123.camundala.services.{ServicesLayers, StandardApp}
import zio.test.Assertion._
import zio.test._

object TwitterAppSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8080/camunda"

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("TwitterAppSuite")(
      testM("compile bpmnModels") {
        for {
          result <- appRunner.update()
        } yield
          assert(result)(isUnit)
      }
    ).provideCustomLayer(ServicesLayers.appDepsLayer >>>
      StandardApp.layer(classOf[TwitterApp], StaticFile("bpmnModels.sc", ".")))
        .mapError(TestFailure.fail)

}
