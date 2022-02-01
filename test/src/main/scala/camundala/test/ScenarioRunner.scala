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
      In <: Product: Encoder: Decoder,
      Out <: Product: Encoder: Decoder
  ](process: Process[In, Out])(
      elements: ElementToTest*
  ): Unit =
    ProcessToTest(process, elements.toList).run()
  lazy val mockedProcess = mock(classOf[ProcessScenario])

  extension [
      In <: Product: Encoder: Decoder,
      Out <: Product: Encoder: Decoder
  ](processToTest: ProcessToTest[In, Out])

    def run(): Unit =
      println(s"PROCESS TO RUN: $processToTest")
      val scenario = prepare()
      exec(scenario)

    private def prepare(): Scenario =
      val ProcessToTest(p: Process[In, Out], elements) = processToTest
      println(s"HEAD: ${p.elements.headOption}")
      elements.foreach {
        case NodeToTest(ut: UserTask[?, ?], _, out) => ut.prepare(out)
        case NodeToTest(st: ServiceTask[?, ?], _, out) => st.prepare(out)
        case NodeToTest(ca: CallActivity[?, ?], _, out) => ca.prepare(out)
        case NodeToTest(dd: DecisionDmn[?, ?], _, _) => dd.prepare()
        case NodeToTest(ee: EndEvent, _, _) => ee.prepare()
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
        case NodeToTest(ut: UserTask[?, ?], _, _) => ut.exec()
        case NodeToTest(st: ServiceTask[?, ?], _, out) => st.exec(out)
        case NodeToTest(ca: CallActivity[?, ?], _, _) => ca.exec()
        case NodeToTest(dd: DecisionDmn[?, ?], _, out) => dd.exec(out)
        case NodeToTest(ee: EndEvent, _, _) => ee.exec()
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
  extension [
      In <: Product: Encoder: Decoder,
      Out <: Product: Encoder: Decoder
  ](process: Process[In, Out])
    def prepare(): Scenario =
      val Process(InOutDescr(id, in, out, descr), _) = process
      Scenario
        .run(mockedProcess)
        .startByKey(process.id, process.in.asJavaVars())
        .execute()

    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance]).isEnded
      checkOutput(process.out.asValueMap())

  end extension

  // UserTasks are mocked
  extension (userTask: UserTask[?, ?])
    def prepare(out: Map[String, Any]): Unit =
      when(mockedProcess.waitsAtUserTask(userTask.id))
        .thenReturn { task =>
          println(s"USERTASK: ${userTask.out}")
          task.complete(out.asJava)
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

    def prepare(out: Map[String, Any]): Unit =
      when(mockedProcess.waitsAtServiceTask(serviceTask.id))
        .thenReturn { task =>
          task.complete(out.asJava)
        }

    def exec(out: Map[String, Any]): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(serviceTask.id)
      checkOutput(out)

  end extension

  // CallActivities are mocked
  extension (callActivity: CallActivity[?, ?])

    def prepare(out: Map[String, Any]): Unit =
      when(mockedProcess.waitsAtMockedCallActivity(callActivity.id))
        .thenReturn { ca =>
          ca.complete(out.asJava)
        }

    def exec(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance])
        .hasPassed(callActivity.id)
  end extension

  // DecisionDmn are executed
  extension (decisionDmn: DecisionDmn[?, ?])
    def prepare(): Unit = ()
    def exec(out: Map[String, Any]): FromProcessInstance[Unit] =
      checkOutput(out)
  end extension

  // EndEvents are passed
  extension (endEvent: EndEvent)
    def prepare(): Unit = ()
    def exec(): FromProcessInstance[Unit] =
      verify(mockedProcess).hasFinished(endEvent.id)
  end extension

end ScenarioRunner
