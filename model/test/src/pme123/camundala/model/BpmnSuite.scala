package pme123.camundala.model

import zio.test.{DefaultRunnableSpec, ZSpec, test, _}
import zio.test.environment.TestEnvironment
import zio.test.Assertion._

object BpmnSuite extends DefaultRunnableSpec{
  override def spec: ZSpec[TestEnvironment, Any] = suite("BpmnSuite")(
    test("Extract Static Files"){
      assert(TestData.bpmn.staticFiles.size)(equalTo(2) ?? "Number of static files")
    },
      test("Generate BPMN"){
        val bpmnStr = TestData.bpmn.generate()
        println(s"BPMN: ${bpmnStr}")
        assert(bpmnStr)(containsString("""("TwitterDemoProcess.bpmn",""")) &&
          assert(bpmnStr)(containsString("""StaticFile("TwitterDemoProcess.bpmn","""))

    }
  )

}
