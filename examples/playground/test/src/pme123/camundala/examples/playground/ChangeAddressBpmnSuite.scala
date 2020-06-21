package pme123.camundala.examples.playground

import pme123.camundala.examples.playground.UsersAndGroups.{complianceGroup, kubeGroup}
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, ZSpec, test, _}

object ChangeAddressBpmnSuite extends DefaultRunnableSpec {
  private val bpmn = ChangeAddressBpmn().ChangeAddressBpmn

  override def spec: ZSpec[TestEnvironment, Any] = suite("BpmnSuite")(
    test("Generate BPMN") {
      val bpmnStr = bpmn.generateDsl()
      println(s"BPMN: ${bpmnStr}")
      assert(bpmnStr)(containsString("lazy val ChangeAddressBpmn: Bpmn =")) &&
        assert(bpmnStr)(containsString("""("ChangeAddress.bpmn",""")) &&
        assert(bpmnStr)(containsString("""BpmnProcess("ChangeAddressDemo")"""))
    }
  )

}
