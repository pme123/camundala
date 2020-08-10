package camundala.tools

import eu.timepit.refined.auto._
import tools.ToolsTestData.bpmns._
import zio.test.Assertion._
import zio.test.{suite, _}

object ToolsSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("ToolsSuite")(
      suite("Create BPMN from XML")(
        testM("create from valid XML") {
          BpmnFromXml.createBpmn("ChangeAddress")
            .map{dsl =>
            val process = dsl.processes.head
              assert(dsl.processes)(
                hasSize(equalTo(1) ?? "processes")
              ) &&
                assert(process.businessRuleTasks)(
                  hasSize(equalTo(1) ?? "businessRuleTasks")
                ) &&
                assert(process.startEvents)(
                  hasSize(equalTo(1) ?? "startEvents")
                ) &&
                assert(process.endEvents)(
                  hasSize(equalTo(1) ?? "endEvents")
                ) &&
                assert(process.userTasks)(
                  hasSize(equalTo(4) ?? "userTasks")
                ) &&
                assert(process.callActivities)(
                  hasSize(equalTo(1) ?? "callActivities")
                ) &&
                assert(process.serviceTasks)(
                  hasSize(equalTo(2) ?? "serviceTasks")
                ) &&
                assert(process.sendTasks)(
                  hasSize(equalTo(0) ?? "sendTasks")
                ) &&
                assert(process.exclusiveGateways)(
                  hasSize(equalTo(3) ?? "exclusiveGateways")
                ) &&
                assert(process.parallelGateways)(
                  hasSize(equalTo(0) ?? "parallelGateways")
                )&&
                assert(process.sequenceFlows)(
                  hasSize(equalTo(15) ?? "sequenceFlows")
                )
            }

        }
      ),
        suite("Generate BPMN Classes")(
        test("generate from valid BPMN") {
          val dsl = GeneratesDsl.generate(SeparatedBpmn1)
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
