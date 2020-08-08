package camundala.tools

import camundala.tools.behaviors.GeneratesDsl
import tools.ToolsTestData.bpmns._
import zio.test.Assertion._
import zio.test._

object ToolsSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("ToolsSuite")(
      suite("Generate BPMN Classes")(
        test("generate from valid BPMN") {
          val dsl = GeneratesDsl.generateBpmns(SeparatedBpmn1, SeparatedBpmn2)
          println(s"DSL: $dsl")
          assert(dsl)(
            containsString("BpmnExample1") &&
              containsString(".processes(") &&
              containsString("SeparatedProcess1,") &&
              containsString("lazy val SeparatedProcess1 =")

          )
        }
      )
    )//.provideLayer(ToolsLayers.toolsLayer).mapError(TestFailure.fail)

}
