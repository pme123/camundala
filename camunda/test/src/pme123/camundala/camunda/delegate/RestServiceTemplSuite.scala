package pme123.camundala.camunda.delegate

import pme123.camundala.camunda.TestData._
import zio.test.Assertion._
import zio.test._

object RestServiceTemplSuite extends DefaultRunnableSpec {


  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("RestServiceTemplSuite")(
        test("create ServiceTask") {
          assert(restServiceTask.extInOutputs.inputs.size)(equalTo(1)) &&
            assert(restServiceTask.extInOutputs.inputs.head.key.value)(equalTo("request"))
        }
      )
}
