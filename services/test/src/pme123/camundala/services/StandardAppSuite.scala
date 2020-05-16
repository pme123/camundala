package pme123.camundala.services

import eu.timepit.refined.auto._
import javax.script.ScriptEngineManager
import pme123.camundala.model.deploy.Url
import zio.test.Assertion._
import zio.test._

object StandardAppSuite extends DefaultRunnableSpec {

  val url: Url = "http://localhost:8080/camunda"

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("StandardAppSuite")(
      test("Script Engine is loaded") {
        assert(Option(new ScriptEngineManager().getEngineByName("scala")).toList)(isNonEmpty)
      }
    )
}
