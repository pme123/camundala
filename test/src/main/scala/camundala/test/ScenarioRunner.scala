package camundala
package test

import camundala.bpmn.*
import camundala.domain.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.scenario.{ProcessScenario, Scenario}
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.util
import scala.jdk.CollectionConverters.*

trait ScenarioRunner extends CommonTesting:

  def test[
      In <: Product,
      Out <: Product
  ](process: Process[In, Out])(
      elements: (ProcessNode | CustomTests)*
  ): Unit =
    ProcessToTest(process, elements.toList).run()

  lazy val mockedProcess = mock(classOf[ProcessScenario])

  extension (processToTest: ProcessToTest[?, ?])

    def run(): Unit =
      val scenario = prepare()
      exec(scenario)

    private def prepare(): Scenario =
      val ProcessToTest(p: Process[?, ?], elements) = processToTest
      println(s"HEAD: ${p.elements.headOption}")
      elements.foreach {
        case ut: UserTask[?, ?] => ut.prepare()
        case st: ServiceTask[?, ?] => st.prepare()
        case dd: DecisionDmn[?, ?] => dd.prepare()
        case ca: CallActivity[?, ?] => ca.prepare()
        case ee: EndEvent => ee.prepare()
        case ct: CustomTests => // nothing to prepare
        case other =>
          throw new IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      p.prepare()

    private def exec(scenario: Scenario): Unit =
      val ProcessToTest(process, elements) = processToTest
      implicit val processInstance = scenario.instance(mockedProcess)
      elements.foreach {
        case ut: UserTask[?, ?] => ut.exec()
        case st: ServiceTask[?, ?] => st.exec()
        case dd: DecisionDmn[?, ?] => dd.exec()
        case ca: CallActivity[?, ?] => ca.exec()
        case ee: EndEvent => ee.exec()
        case ct: CustomTests => // not supported
          println("CustomTests are not supported!")
        case other =>
          throw IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      process.exec()

  end extension

  // Processes are executed
  extension (process: Process[?, ?])
    def prepare(): Scenario =
      val Process(InOutDescr(id, in, out, descr), _) = process
      Scenario
        .run(mockedProcess)
        .startByKey(process.id, process.in.asJavaVars())
        .execute()

    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance]).isEnded
      checkOutput(process.out)

  end extension

  // UserTasks are mocked
  extension (userTask: UserTask[?, ?])
    def prepare(): Unit = 
      when(mockedProcess.waitsAtUserTask(userTask.id))
        .thenReturn { task =>
          println(s"USERTASK: ${userTask.out}")
          task.complete(userTask.out.asJavaVars())
        }

    def exec(): FromProcessInstance[Unit] = ()
   /*   val UserTask(InOutDescr(id, in, out, descr)) = userTask
      val t = task()
      assertThat(t)
        .hasDefinitionKey(id)
      BpmnAwareTests.complete(t, out.asJavaVars())
      assertThat(summon[CProcessInstance])
        .hasPassed(id) */
  end extension

  // ServiceTasks are mocked
  extension (serviceTask: ServiceTask[?, ?])

    def prepare(): Unit =
      when(mockedProcess.waitsAtServiceTask(serviceTask.id))
        .thenReturn { task =>
          task.complete(serviceTask.out.asJavaVars())
        }

    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(serviceTask.id)
      checkOutput(serviceTask.out)

  end extension

  // CallActivities are mocked
  extension (callActivity: CallActivity[?, ?])
    def prepare(): Unit =
      when(mockedProcess.waitsAtMockedCallActivity(callActivity.id))
        .thenReturn { ca =>
          ca.complete(callActivity.out.asJavaVars())
        }
    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(callActivity.id)
  end extension

  // DecisionDmn are executed
  extension (decisionDmn: DecisionDmn[?, ?])
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] =
      checkOutput(decisionDmn.out)
  end extension

  // EndEvents are passed
  extension (endEvent: EndEvent)
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] =
      verify(mockedProcess).hasFinished(endEvent.id)
  end extension

end ScenarioRunner
