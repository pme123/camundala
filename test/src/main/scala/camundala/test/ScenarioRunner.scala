package camundala
package test

import camundala.bpmn.*
import camundala.domain.*
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.test.TestHelper
import org.camunda.bpm.engine.runtime.{Job, ProcessInstance}
import org.camunda.bpm.engine.test.assertions.bpmn.AbstractAssertions.processEngine
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.test.mock.Mocks
import org.camunda.bpm.engine.test.{ProcessEngineRule, ProcessEngineTestCase}
import org.camunda.bpm.engine.variable.impl.VariableMapImpl
import org.camunda.bpm.extension.mockito.CamundaMockito.registerCallActivityMock
import org.camunda.bpm.scenario.{ProcessScenario, Scenario}
import org.junit.Assert.{assertEquals, assertNotNull, fail}
import org.junit.{Before, Rule}
import org.mockito.Mockito.{mock, verify, when}
import org.mockito.MockitoAnnotations

import java.io.FileNotFoundException
import java.util
import scala.jdk.CollectionConverters.*

trait ScenarioRunner extends CommonTesting :

  def mockSubProcess[
    In <: Product : Encoder : Decoder,
    Out <: Product : Encoder : Decoder
  ](process: Process[In, Out]): Unit =
    processEngineRule.manageDeployment(
      registerCallActivityMock(process.id)
        .onExecutionAddVariables(
          new VariableMapImpl(process.out.asJavaVars())
        )
        .deploy(processEngine())
    )

  def test[
    In <: Product : Encoder : Decoder,
    Out <: Product : Encoder : Decoder
  ](process: Process[In, Out])(
    elements: ElementToTest*
  ): Unit =
    ProcessToTest(process, elements.toList).run()

  lazy val mockedProcess = mock(classOf[ProcessScenario])

  extension[
    In <: Product : Encoder : Decoder,
    Out <: Product : Encoder : Decoder
  ] (processToTest: ProcessToTest[In, Out])

    def run(): Unit =
      println(s"PROCESS TO RUN: $processToTest")
      prepare()
      val scenario = processToTest.process.run()
      check(scenario)

    private def prepare(): Unit =
      val ProcessToTest(p: Process[In, Out], elements) = processToTest
      val prepElements: Map[String, Map[String, Seq[NodeToTest]]] = elements
        .collect { case nt: NodeToTest => nt }
        .groupBy(_.inOut.getClass.getSimpleName)
        .map { case k -> v => k -> v.groupBy(_.inOut.id) }
      prepElements
        .foreach {
          case "UserTask" -> testSteps => testSteps.foreach {
            case id -> nodes => prepareUserTask(id, nodes)
          }
          case "ServiceTask" -> testSteps => testSteps.foreach {
            case id -> nodes => prepareServiceTask(id, nodes)
          }
          case "CallActivity" -> testSteps => testSteps.foreach {
            case id -> nodes => prepareCallActivity(id, nodes)
          }
          case "DecisionDmn" -> _ => () // no preparation
          case "EndEvent" -> _ => () // no preparation
          case other =>
            throw new IllegalArgumentException(
              s"This TestNodes are not supported: $other"
            )
        }

    private def check(scenario: Scenario): Unit =
      val ProcessToTest(process, elements) = processToTest
      implicit val processInstance = scenario.instance(mockedProcess)
      elements.foreach {
        case NodeToTest(ut: UserTask[?, ?], _, _) =>
          hasPassed(ut.id)
        case NodeToTest(st: ServiceTask[?, ?], _, _) =>
          hasPassed(st.id)
        case NodeToTest(ca: CallActivity[?, ?], _, _) =>
          hasPassed(ca.id)
        case NodeToTest(dd: DecisionDmn[?, ?], _, _) =>
          hasPassed(dd.id)
        case NodeToTest(ee: EndEvent, _, _) =>
          hasFinished(ee.id)
        case ct: CustomTests => // not supported
          println("CustomTests are not supported!")
        case other =>
          throw IllegalArgumentException(
            s"This TestStep is not supported: $other"
          )
      }
      process.check()

  end extension

  def prepareUserTask(id: String, nodes: Seq[NodeToTest]): Unit =
    val iter = nodes.iterator
    when(mockedProcess.waitsAtUserTask(id))
      .thenReturn { task =>
        val node = iter.nextOption()
        if (node.isEmpty)
          fail(s"There is no UserTask expected for $id")
        println(s"UserTask $id: ${node.get.out}")
        assertThat(task)
          .hasDefinitionKey(id)
        task.complete(node.get.out.asJava)
      }

  def prepareServiceTask(id: String, nodes: Seq[NodeToTest]): Unit =
    val iter = nodes.iterator
    when(mockedProcess.waitsAtServiceTask(id))
      .thenReturn { task =>
        val node = iter.nextOption()
        if (node.isEmpty)
          fail(s"There is no ServiceTask expected for $id")
        println(s"ServiceTask $id: ${node.get.out}")
        task.complete(node.get.out.asJava)
      }

  def prepareCallActivity(id: String, nodes: Seq[NodeToTest]): Unit =
    val iter = nodes.iterator
    when(mockedProcess.waitsAtMockedCallActivity(id))
      .thenReturn { ca =>
        val node = iter.nextOption()
        if (node.isEmpty)
          fail(s"There is no CallActivity expected for $id")
        println(s"CallActivity $id: ${node.get.out}")
        ca.complete(node.get.out.asJava)
      }

  def hasPassed(id: String): FromProcessInstance[Unit] =
    assertThat(summon[CProcessInstance])
      .hasPassed(id)

  def hasFinished(id: String): FromProcessInstance[Unit] =
    verify(mockedProcess).hasFinished(id)

  // Processes are executed
  extension[
    In <: Product : Encoder : Decoder,
    Out <: Product : Encoder : Decoder
  ] (process: Process[In, Out])
    def run(): Scenario =
      val Process(InOutDescr(id, in, out, descr), _) = process
      Scenario
        .run(mockedProcess)
        .startByKey(process.id, process.in.asJavaVars())
        .execute()

    def check(): FromProcessInstance[Unit] =
      assertThat(summon[CProcessInstance]).isEnded
      checkOutput(process.out.asValueMap())

  end extension


end ScenarioRunner
