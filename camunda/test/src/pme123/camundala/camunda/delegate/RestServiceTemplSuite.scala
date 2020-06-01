package pme123.camundala.camunda.delegate

import pme123.camundala.camunda.TestData._
import pme123.camundala.camunda.service.restService.Request.Host
import zio.test.Assertion._
import zio.test._

object RestServiceTemplSuite extends DefaultRunnableSpec {


  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("RestServiceTemplSuite")(
        test("create ServiceTask") {
          assert(restServiceTask.extensions.inOuts.inputs.size)(equalTo(1)) &&
            assert(restServiceTask.extensions.inOuts.inputs.head.key.value)(equalTo("request"))
        }
      )
}
