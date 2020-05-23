package pme123.camundala.camunda.delegate

import eu.timepit.refined.auto._
import pme123.camundala.camunda.TestData._
import pme123.camundala.camunda.delegate.RestServiceDelegate.RestServiceTempl
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.deploy.Sensitive
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
