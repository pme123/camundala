package pme123.camundala.camunda

import pme123.camundala.camunda.bpmn.ValidateWarnings
import pme123.camundala.model._
import zio.test.Assertion.equalTo
import zio.test._

import scala.io.Source
import scala.xml._

object BpmnServiceSuite extends DefaultRunnableSpec {

  private val bpmn = Source.fromResource("bpmn/TwitterModelProcess.bpmn")
  private val process: BpmnProcess = BpmnProcess("TwitterDemoProcess",
    List(
      UserTask("user_task_review_tweet",
        Extensions(Map("durationMean" -> "10000", "durationSd" -> "5000")))),
    List(
      ServiceTask("service_task_send_rejection_notification",
        Extensions(Map("KPI-Ratio" -> "Tweet Rejected"))),
      ServiceTask("service_task_publish_on_twitter",
        Extensions(Map("KPI-Ratio" -> "Tweet Approved")))
    ))
  private val expected = Bpmn(List(
    process))

  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("BpmnSuite")(
      testM("the BPMN Model is merged") {
        for {
          _ <- processRegister.registerProcess(process)
          mergeResult <- bpmnService.mergeBpmn(XML.load(bpmn.reader()))
          userTasks = mergeResult.xmlNode \\ "userTask"
          serviceTasks = mergeResult.xmlNode \\ "serviceTask"
          userTaskProps = userTasks.head \\ "property"
          serviceTaskProp = serviceTasks.head \\ "property"
        } yield {
          assert(mergeResult.warnings)(equalTo(ValidateWarnings.none)) &&
            assert(userTasks.size)(equalTo(1)) &&
            assert(userTaskProps.length)(equalTo(3))  &&
            assert(serviceTasks.length)(equalTo(2)) &&
          assert(serviceTaskProp.length) (equalTo(1))
        }
      }
    ).provideCustomLayer(((processRegister.live >>> bpmnService.live) ++ processRegister.live).mapError(TestFailure.fail))
}
