package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import zio.test._
import zio.test.Assertion._
import zio.test.environment.TestEnvironment

object StaticFileSuite extends DefaultRunnableSpec {

  override def spec: ZSpec[TestEnvironment, Any] =
    suite("StaticFileSuite")(
      test("file name without extension") {
        assert(StaticFile("dmn/my-dmn.ok.dmn").fileNameWithoutExtension)(equalTo("dmn/my-dmn.ok"))
      }
    )
}
