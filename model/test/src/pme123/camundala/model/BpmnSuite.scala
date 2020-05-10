package pme123.camundala.model

import zio.test.{DefaultRunnableSpec, ZSpec}
import zio.test.environment.TestEnvironment
import zio.test._
import zio.test.Assertion._

object BpmnSuite extends DefaultRunnableSpec{
  override def spec: ZSpec[TestEnvironment, Any] = suite("BpmnSuite")(
    test("Extract Static Files"){
      assert(TestData.bpmn.staticFiles.size)(equalTo(2) ?? "Number of static files")
    }
  )

}
