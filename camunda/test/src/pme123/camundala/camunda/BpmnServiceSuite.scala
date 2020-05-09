package pme123.camundala.camunda

import org.junit.runner.RunWith
import pme123.camundala.camunda.xml.XmlHelper
import pme123.camundala.camunda.xml.XmlHelper.XQualifier
import pme123.camundala.model.bpmn.bpmnRegister
import zio.test.Assertion._
import zio.test._
import zio.test.junit.ZTestJUnitRunner
import zio.{Task, UIO, ZIO, ZManaged}

import scala.xml._

@RunWith(classOf[ZTestJUnitRunner])
object BpmnServiceSuite extends DefaultRunnableSpec {

  import TestData._

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is merged") {
        for {
          bpmnXml <- bpmnXmlTask
          _ <- bpmnRegister.registerBpmn(bpmn)
          mergeResult <- bpmnService.mergeBpmn("TwitterDemoProcess.bpmn", bpmnXml)
          userTasks = mergeResult.xmlElem \\ "userTask"
          serviceTasks = mergeResult.xmlElem \\ "serviceTask"
          sendTasks = mergeResult.xmlElem \\ "sendTask"
          startEvents = mergeResult.xmlElem \\ "startEvent"
          exclusiveGateways = mergeResult.xmlElem \\ "exclusiveGateway"
          userTaskProps = userTasks.head \\ "property"
          delegateExpression = XQualifier.camunda(XmlHelper.delegateExpression).extractFrom(serviceTasks.head)
          externalTask = XQualifier.camunda("topic").extractFrom(serviceTasks.last)
          serviceTaskProp = serviceTasks.head \\ "property"
          startEventProp = startEvents \\ "property"
          exclusiveGatewayProp = exclusiveGateways.head \\ "property"
        } yield {
          assert(mergeResult.warnings.value.length)(equalTo(2) ?? "warnings") &&
            assert(mergeResult.warnings.value.head.msg)(equalTo("You have 2 ExclusiveGateway in the XML-Model, but you have 1 in Scala")) &&
            assert(mergeResult.warnings.value(1).msg)(equalTo("There is NOT a ExclusiveGateway with id 'gateway_join' in Scala.")) &&
            assert(userTasks.size)(equalTo(1) ?? "userTasks") &&
            assert(userTaskProps.length)(equalTo(3) ?? "userTaskProps") &&
            assert(serviceTasks.length)(equalTo(3) ?? "serviceTasks") &&
            assert(sendTasks.length)(equalTo(1) ?? "sendTasks") &&
            assert(serviceTaskProp.length)(equalTo(1) ?? "serviceTaskProp") &&
            assert(startEvents.length)(equalTo(2) ?? "startEvents") &&
            assert(startEventProp.length)(equalTo(1) ?? "startEventProp") &&
            assert(exclusiveGateways.length)(equalTo(2) ?? "exclusiveGateways") &&
            assert(exclusiveGatewayProp.length)(equalTo(1) ?? "exclusiveGatewayProp") &&
            assert(delegateExpression)(isSome(equalTo("#{emailAdapter}"))) &&
            assert(externalTask)(isSome(equalTo("myTopic")))

        }
      },
      testM("Validate a BPMN") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          valWarns <- bpmnService.validateBpmn("TwitterDemoProcess.bpmn")
        } yield
          assert(valWarns.value.size)(equalTo(2))
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live) ++ bpmnRegister.live).mapError(TestFailure.fail))

}
