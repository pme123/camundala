package pme123.camundala.services

import eu.timepit.refined.auto._
import javax.script.ScriptEngineManager
import pme123.camundala.model.bpmn.Url
import zio.test.Assertion._
import zio.test._
import zio.test.TestAspect.ignore

object StandardAppSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8080/camunda"

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("StandardAppSuite")(
      test("Script Engine is loaded") {
        assert(Option(new ScriptEngineManager().getEngineByName("scala")).toList)(isNonEmpty)
      }@@ ignore // see https://stackoverflow.com/questions/62131981/scala-script-engine-is-not-found-when-run-in-test-using-mill

    )
}
