package pme123.camundala.camunda


import eu.timepit.refined.auto._
import pme123.camundala.camunda.xml.XmlHelper
import pme123.camundala.camunda.xml.XmlHelper.XQualifier
import pme123.camundala.model.register.bpmnRegister
import zio.test.Assertion._
import zio.test._

object BpmnServiceSuite extends DefaultRunnableSpec {

  import TestData._

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is merged") {
        for {
          bpmnXml <- bpmnXmlTask
          _ <- bpmnRegister.registerBpmn(bpmn)
          mergeResult <- bpmnService.mergeBpmn("TwitterDemoProcess.bpmn", bpmnXml)
          _ <- zio.console.putStrLn(s"BPMN XML ${mergeResult.xmlElem}")
          _ <- zio.console.putStrLn(s"BPMN WARNINGS ${mergeResult.warnings.value.mkString("\n")}")
          userTasks = mergeResult.xmlElem \\ "userTask"
          userTaskInputParam = mergeResult.xmlElem \\ "userTask" \\ "inputParameter"
          userTaskForm = XQualifier.camunda("formKey").extractFrom(userTasks.head)
          serviceTasks = mergeResult.xmlElem \\ "serviceTask"
          sendTasks = mergeResult.xmlElem \\ "sendTask"
          startEvents = mergeResult.xmlElem \\ "startEvent"
          startEventForm = XQualifier.camunda("formKey").extractFrom(startEvents.head)
          exclusiveGateways = mergeResult.xmlElem \\ "exclusiveGateway"
          userTaskProps = userTasks.head \\ "property"
          delegateExpression = XQualifier.camunda(XmlHelper.delegateExpression).extractFrom(serviceTasks.head)
          externalTask = XQualifier.camunda("topic").extractFrom(serviceTasks.last)
          serviceTaskProp = serviceTasks.head \\ "property"
          startEventProp = startEvents \\ "property"
          exclusiveGatewayProp = exclusiveGateways.head \\ "property"
          sequenceFlowProps = mergeResult.xmlElem \\ "sequenceFlow" \\ "property"
          sequenceFlowConditionals = mergeResult.xmlElem \\ "sequenceFlow" \\ "conditionExpression"

        } yield {
          assert(mergeResult.warnings.value.length)(equalTo(13) ?? "warnings") &&
            assert(mergeResult.warnings.value.head.msg)(equalTo("You have 2 ExclusiveGateway in the XML-Model, but you have 1 in Scala")) &&
            assert(mergeResult.warnings.value(1).msg)(equalTo("There is NOT a ExclusiveGateway with id 'gateway_join' in Scala.")) &&
            assert(userTasks.size)(equalTo(1) ?? "userTasks") &&
            assert(userTaskForm)(isSome(equalTo("embedded:deployment:static/forms/reviewTweet.html"))) &&
            assert(userTaskProps.length)(equalTo(3) ?? "userTaskProps") &&
            assert(serviceTasks.length)(equalTo(4) ?? "serviceTasks") &&
            assert(sendTasks.length)(equalTo(1) ?? "sendTasks") &&
            assert(serviceTaskProp.length)(equalTo(1) ?? "serviceTaskProp") &&
            assert(startEvents.length)(equalTo(2) ?? "startEvents") &&
            assert(startEventForm)(isSome(equalTo("embedded:deployment:static/forms/createTweet.html"))) &&
            assert(startEventProp.length)(equalTo(1) ?? "startEventProp") &&
            assert(exclusiveGateways.length)(equalTo(2) ?? "exclusiveGateways") &&
            assert(exclusiveGatewayProp.length)(equalTo(1) ?? "exclusiveGatewayProp") &&
            assert(delegateExpression)(isSome(equalTo("#{emailAdapter}"))) &&
            assert(externalTask)(isSome(equalTo("myTopic"))) &&
            assert(sequenceFlowProps.length)(equalTo(2) ?? "sequenceFlow Properties count") &&
            assert(sequenceFlowConditionals.length)(equalTo(2) ?? "sequenceFlow Conditionals count") &&
            assert(userTaskInputParam.length)(equalTo(2) ?? "userTaskInputParam")
        }
      },
      testM("Validate a BPMN") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          valWarns <- bpmnService.validateBpmn("TwitterDemoProcess.bpmn")
        } yield
          assert(valWarns.value.size)(equalTo(13) ?? "warnings")
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live) ++ bpmnRegister.live).mapError(TestFailure.fail))

}
