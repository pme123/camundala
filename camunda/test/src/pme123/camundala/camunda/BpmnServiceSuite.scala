package pme123.camundala.camunda


import eu.timepit.refined.auto._
import pme123.camundala.camunda.xml.XmlHelper
import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.ModelLayers
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
          processes = mergeResult.xmlElem \\ "process"
          userTasks = mergeResult.xmlElem \\ "userTask"
          userTaskInputParam = mergeResult.xmlElem \\ "userTask" \\ "inputParameter"
          userTaskForm = userTasks.head.attributeAsText(QName.camunda("formKey"))
          serviceTasks = mergeResult.xmlElem \\ "serviceTask"
          sendTasks = mergeResult.xmlElem \\ "sendTask"
          startEvents = mergeResult.xmlElem \\ "startEvent"
          startEventForm = startEvents.head.attributeAsText(QName.camunda("formKey"))
          exclusiveGateways = mergeResult.xmlElem \\ "exclusiveGateway"
          userTaskProps = userTasks.head \\ "property"
          delegateExpression = serviceTasks.head.attributeAsText(QName.camunda(XmlHelper.delegateExpression))
          externalTask = serviceTasks.last.attributeAsText(QName.camunda("topic"))
          serviceTaskProp = serviceTasks.head \\ "property"
          startEventProp = startEvents \\ "property"
          exclusiveGatewayProp = exclusiveGateways.head \\ "property"
          sequenceFlowProps = mergeResult.xmlElem \\ "sequenceFlow" \\ "property"
          sequenceFlowConditionals = mergeResult.xmlElem \\ "sequenceFlow" \\ "conditionExpression"
          _ = println(s"userTasks.head: ${userTasks.head.attribute(xmlnsCamunda, candidateUsers)}")
        } yield {
          assert(mergeResult.warnings.value.length)(equalTo(13) ?? "warnings") &&
            assert(mergeResult.warnings.value.head.msg)(equalTo("You have 2 ExclusiveGateway in the XML-Model, but you have 1 in Scala")) &&
            assert(mergeResult.warnings.value(1).msg)(equalTo("There is NOT a ExclusiveGateway with id 'gateway_join' in Scala.")) &&
            assert(processes.size)(equalTo(2) ?? "processes") &&
            assert(processes.head \@ "id")(equalTo("TwitterDemoProcess")) &&
            assert(processes.head.attribute(xmlnsCamunda, candidateStarterUsers).get.head.text)(equalTo("heidi,peter,alina") ?? "candidateStarterUsers") &&
            assert(processes.head.attribute(xmlnsCamunda, candidateStarterGroups).get.head.text)(equalTo("worker,guest") ?? "candidateStarterUsers") &&
            assert(userTasks.size)(equalTo(1) ?? "userTasks") &&
            assert(userTaskForm)(equalTo("embedded:deployment:static/forms/reviewTweet.html")) &&
            assert(userTaskProps.length)(equalTo(3) ?? "userTaskProps") &&
            assert(userTasks.head \@ "id")(equalTo("user_task_review_tweet")) &&
            assert(userTasks.head.attribute(xmlnsCamunda, candidateUsers).get.head.text)(equalTo("heidi") ?? "candidateUsers") &&
            assert(userTasks.head.attribute(xmlnsCamunda, candidateGroups).get.head.text)(equalTo("worker,player") ?? "candidateGroups") &&
            assert(serviceTasks.length)(equalTo(4) ?? "serviceTasks") &&
            assert(sendTasks.length)(equalTo(1) ?? "sendTasks") &&
            assert(serviceTaskProp.length)(equalTo(1) ?? "serviceTaskProp") &&
            assert(startEvents.length)(equalTo(2) ?? "startEvents") &&
            assert(startEventForm)(equalTo("embedded:deployment:static/forms/createTweet.html")) &&
            assert(startEventProp.length)(equalTo(1) ?? "startEventProp") &&
            assert(exclusiveGateways.length)(equalTo(2) ?? "exclusiveGateways") &&
            assert(exclusiveGatewayProp.length)(equalTo(1) ?? "exclusiveGatewayProp") &&
            assert(delegateExpression)(equalTo("#{emailAdapter}")) &&
            assert(externalTask)(equalTo("myTopic")) &&
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
      },
      testM("the BPMN Model is generated") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          paths <- bpmnService.generateBpmn("TwitterDemoProcess.bpmn")
        } yield {
          assert(paths.length)(equalTo(3) ?? "number of generated Paths")
        }
      },

    ).provideCustomLayer((CamundaLayers.bpmnServiceLayer ++ ModelLayers.bpmnRegisterLayer).mapError(TestFailure.fail))

}
