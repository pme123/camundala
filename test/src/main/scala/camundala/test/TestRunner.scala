package camundala
package test

import domain.*
import bpmn.*
import io.circe.syntax.*
import io.circe.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{assertThat, managementService, repositoryService, runtimeService, task}
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.variable.value.TypedValue
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

import scala.jdk.CollectionConverters.*
import java.io.FileNotFoundException
import java.util

trait TestRunner extends CommonTesting:
  import io.circe.syntax.*
  def test[
    In <: Product: Encoder: Decoder,
    Out <: Product: Encoder: Decoder
  ](process: Process[In, Out])(
      activities: ElementToTest*
  ): Unit =
    ProcessToTest(process, activities.toList).run()


  extension [
    In <: Product: Encoder: Decoder,
    Out <: Product: Encoder: Decoder
  ](processToTest: ProcessToTest[In, Out])
    def run(): Unit =
      val ProcessToTest(
        Process(InOutDescr(id, in, out, descr), _),
        elements
      ) = processToTest
      implicit val processInstance: CProcessInstance = runtimeService.startProcessInstanceByKey(
        id,
        in.asJavaVars()
      )
      assertThat(processInstance)
        .isStarted()
      // run manual tasks
      elements.foreach {
        case NodeToTest(ut: UserTask[?, ?], _, out) => ut.run(out)
        case NodeToTest(st: ServiceTask[?, ?], _, _) => st.run()
        case NodeToTest(ca: CallActivity[?, ?], _, _) => ca.run()
        case NodeToTest(dd: DecisionDmn[?, ?], _, out) => dd.run(out)
        case NodeToTest(ee: EndEvent, _, _) => ee.run()
        case ct: CustomTests => ct.tests()
        case other =>
          throw new IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      checkOutput(out.asValueMap())
      assertThat(processInstance).isEnded
  end extension

  extension (userTask: UserTask[?, ?])

    def run(out: Map[String, Any]): FromProcessInstance[Unit] =
      val t = task()
      assertThat(t)
        .hasDefinitionKey(userTask.id)
      /*    userTask.maybeForm.foreach {
        case EmbeddedForm(formKey) =>
          assertThat(t).hasFormKey(formKey.toString)
        case _ => // nothing to test
      }
      userTask.maybeAssignee.foreach(assignee =>
        assertThat(t).isAssignedTo(assignee.toString)
      )
      userTask.candidateGroups.groups.foreach(group =>
        assertThat(t).hasCandidateGroup(group.toString)
      )
      userTask.candidateUsers.users.foreach(user =>
        assertThat(t).hasCandidateUser(user.toString)
      )
      userTask.maybePriority.foreach(prio =>
        assertEquals(prio, t.getPriority) // no assertThat
      )
      userTask.maybeDueDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
      )
      userTask.maybeFollowUpDate.foreach(date =>
        assertThat(t).hasDueDate(toCamundaDate(date.expression))
      )
       */
      BpmnAwareTests.complete(t, out.asJava)
      assertThat(summon[CProcessInstance])
        .hasPassed(userTask.id)
  end extension

  extension (serviceTask: ServiceTask[?, ?])
    def run(): FromProcessInstance[Unit] =
      val archiveInvoiceJob = managementService.createJobQuery.singleResult
      assertNotNull(archiveInvoiceJob)
      managementService.executeJob(archiveInvoiceJob.getId)
      assertThat(summon[CProcessInstance])
        .hasPassed(serviceTask.id)
  end extension

  extension (callActivity: CallActivity[?, ?])
    def run(): FromProcessInstance[Unit] = ()
      //checkOutput(out)
  end extension

  extension (decisionDmn: DecisionDmn[?, ?])
    def run(out: Map[String, Any]): FromProcessInstance[Unit] =
    //  assertThat(summon[CProcessInstance])
    //    .hasPassed(decisionDmn.id)
      checkOutput(out)
  end extension

  extension (endEvent: EndEvent)
    def run(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(endEvent.id)
  end extension

end TestRunner
