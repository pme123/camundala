package camundala
package test

import domain.*
import bpmn.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.{assertThat, managementService, repositoryService, runtimeService, task}
import org.camunda.bpm.engine.test.mock.Mocks
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.util

trait TestRunner extends CommonTesting:

  def test[
      In <: Product,
      Out <: Product
  ](process: Process[In, Out])(
      activities: (ProcessNode | CustomTests)*
  ): Unit =
    ProcessToTest(process, activities.toList).run()


  extension (processToTest: ProcessToTest[?, ?])
    def run(): Unit =
      val ProcessToTest(
        Process(InOutDescr(id, in, out, descr), _),
        elements
      ) = processToTest
      implicit val processInstance = runtimeService.startProcessInstanceByKey(
        id,
        in.asJavaVars()
      )
      assertThat(processInstance)
        .isStarted()
      // run manual tasks
      elements.foreach {
        case ut: UserTask[?, ?] => ut.run()
        case st: ServiceTask[?, ?] => st.run()
        case dd: DecisionDmn[?, ?] => dd.run()
        case ca: CallActivity[?, ?] => ca.run()
        case ee: EndEvent => ee.run()
        //(a: Activity[?,?,?]) => a.run(processInstance)
        case ct: CustomTests => ct.tests()
        case other =>
          throw IllegalArgumentException(
            s"This Activity is not supported: $other"
          )
      }
      checkOutput(out)
      assertThat(processInstance).isEnded
  end extension

  extension (userTask: UserTask[?, ?])
    def run(): FromProcessInstance[Unit] =
      val UserTask(InOutDescr(id, in, out, descr)) = userTask
      val t = task()
      assertThat(t)
        .hasDefinitionKey(id)
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
      BpmnAwareTests.complete(t, out.asJavaVars())
      assertThat(summon[CProcessInstance])
        .hasPassed(id)
  end extension

  extension (serviceTask: ServiceTask[?, ?])
    def run(): FromProcessInstance[Unit] =
      val ServiceTask(InOutDescr(id, in, out, descr)) = serviceTask
      val archiveInvoiceJob = managementService.createJobQuery.singleResult
      assertNotNull(archiveInvoiceJob)
      managementService.executeJob(archiveInvoiceJob.getId)
      assertThat(summon[CProcessInstance])
        .hasPassed(id)
  end extension

  extension (callActivity: CallActivity[?, ?])
    def run(): FromProcessInstance[Unit] =
      val CallActivity(InOutDescr(id, in, out, descr)) =
        callActivity
      //checkOutput(out)
  end extension

  extension (decisionDmn: DecisionDmn[?, ?])
    def run(): FromProcessInstance[Unit] =
      val DecisionDmn(InOutDescr(id, in, out, descr)) =
        decisionDmn
      checkOutput(out)
  end extension

  extension (endEvent: EndEvent)
    def run(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(endEvent.id)
  end extension

end TestRunner
