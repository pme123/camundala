package pme123.camundala.camunda

import pme123.camundala.model._
import zio.test.Assertion.equalTo
import zio.test._

import scala.collection.immutable.HashSet
import scala.io.Source
import scala.xml._

object BpmnServiceSuite extends DefaultRunnableSpec {

  private val bpmnResource = Source.fromResource("bpmn/TwitterModelProcess.bpmn")
  private val bpmn = Bpmn("TwitterDemoProcess.bpmn",
    List(
      BpmnProcess("TwitterDemoProcess",
        List(
          UserTask("user_task_review_tweet",
            Extensions(Map("durationMean" -> "10000", "durationSd" -> "5000")))),
        List(
          ServiceTask("service_task_send_rejection_notification",
            Extensions(Map("KPI-Ratio" -> "Tweet Rejected"))),
          ServiceTask("service_task_publish_on_twitter",
            Extensions(Map("KPI-Ratio" -> "Tweet Approved")))
        ),
        List(StartEvent("start_event_new_tweet",
          Extensions(Map("KPI-Cycle-Start" -> "Tweet Approval Time"))
        )),
        List(ExclusiveGateway("gateway_approved",
          Extensions(Map("KPI-Cycle-End" -> "Tweet Approval Time"))
        )
        ),
      )), HashSet(
      StaticFile("bpmn/TwitterModelProcess.bpmn")
    ))

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is merged") {
        for {
          _ <- bpmnRegister.registerBpmn(bpmn)
          mergeResult <- bpmnService.mergeBpmn("TwitterDemoProcess.bpmn", XML.load(bpmnResource.reader()))
          userTasks = mergeResult.xmlNode \\ "userTask"
          serviceTasks = mergeResult.xmlNode \\ "serviceTask"
          startEvents = mergeResult.xmlNode \\ "startEvent"
          exclusiveGateways = mergeResult.xmlNode \\ "exclusiveGateway"
          userTaskProps = userTasks.head \\ "property"
          serviceTaskProp = serviceTasks.head \\ "property"
          startEventProp = startEvents \\ "property"
          exclusiveGatewayProp = exclusiveGateways.head \\ "property"
        } yield {
          assert(mergeResult.warnings.value.length)(equalTo(2) ?? "warnings") &&
            assert(mergeResult.warnings.value.head.msg)(equalTo("You have 2 ExclusiveGateway in the XML-Model, but you have 1 in Scala")) &&
            assert(mergeResult.warnings.value(1).msg)(equalTo("There is NOT a ExclusiveGateway with id 'gateway_join' in Scala.")) &&
            assert(userTasks.size)(equalTo(1) ?? "userTasks") &&
            assert(userTaskProps.length)(equalTo(3) ?? "userTaskProps") &&
            assert(serviceTasks.length)(equalTo(2) ?? "serviceTasksserviceTasks") &&
            assert(serviceTaskProp.length)(equalTo(1) ?? "serviceTaskProp") &&
            assert(startEvents.length)(equalTo(1) ?? "startEvents") &&
            assert(startEventProp.length)(equalTo(1) ?? "startEventProp") &&
            assert(exclusiveGateways.length)(equalTo(2) ?? "exclusiveGateways") &&
            assert(exclusiveGatewayProp.length)(equalTo(1) ?? "exclusiveGatewayProp")
        }
      }
    ).provideCustomLayer(((bpmnRegister.live >>> bpmnService.live) ++ bpmnRegister.live).mapError(TestFailure.fail))
}
