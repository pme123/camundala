package pme123.camundala.camunda

import io.circe.parser._
import pme123.camundala.camunda.httpDeployClient._
import pme123.camundala.model.register.bpmnRegister
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test._

object HttpDeployClientSuite extends DefaultRunnableSpec {

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("HttpDeployClientSuite")(
      testM("Decode Camunda Response") {
        for {
          json <- ZIO.fromEither(parse(jsonStr))
          result <- ZIO.fromEither(json.as[DeployResult])
        } yield
          assert(result.id)(equalTo("7aa1900f-9739-11ea-bf92-0242ac130005")) &&
        assert(result.tenantId)(equalTo(None))
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live) ++ bpmnRegister.live).mapError(TestFailure.fail))

   val jsonStr = """{"links":[{"method":"GET","href":"http://localhost:8085/engine-rest/deployment/7aa1900f-9739-11ea-bf92-0242ac130005","rel":"self"}],"id":"7aa1900f-9739-11ea-bf92-0242ac130005","name":"TwitterDemoProcess.bpmn","source":"Camundala Client","deploymentTime":"2020-05-16T07:52:51.454+0200","tenantId":null,"deployedProcessDefinitions":{"TwitterDemoProcess:2:7ab60273-9739-11ea-bf92-0242ac130005":{"id":"TwitterDemoProcess:2:7ab60273-9739-11ea-bf92-0242ac130005","key":"TwitterDemoProcess","category":"http://www.signavio.com/bpmn20","description":null,"name":"TwitterDemoProcess","version":2,"resource":"TwitterDemoProcess.bpmn","deploymentId":"7aa1900f-9739-11ea-bf92-0242ac130005","diagram":null,"suspended":false,"tenantId":null,"versionTag":null,"historyTimeToLive":null,"startableInTasklist":true}},"deployedCaseDefinitions":null,"deployedDecisionDefinitions":null,"deployedDecisionRequirementsDefinitions":null}"""
}



