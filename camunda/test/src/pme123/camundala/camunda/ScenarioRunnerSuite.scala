package pme123.camundala.camunda

import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests._
import org.camunda.bpm.engine.test.{Deployment, ProcessEngineRule}
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.{Rule, Test}
import pme123.camundala.model.scenarios.ProcessScenario
import zio.test.Assertion._
import zio.test._
import zio._
import scala.annotation.meta.getter
import scala.jdk.CollectionConverters._
import zio.Runtime.default.unsafeRun

@RunWith(classOf[JUnit4])
class ScenarioRunnerSuite {

  import TestData._

  @(Rule@getter)
  val processEngineRule: ProcessEngineRule = new ProcessEngineRule()
  lazy val processInstance = runtimeService.startProcessInstanceByKey("myProcess Id",  Map[String, AnyRef]("customer" -> "meier").asJava)

  @Test
  def testHappyPath(): Unit = {
    unsafeRun(
      (for {
        result <- scenarioRunner.run(ProcessScenario("myScenario", twitterProcess))
        _ = println(s"Scenario Result: $result")

      } yield {
        assert(true)(equalTo(true) ?? "scenario Dummy")
      }).provideCustomLayer(CamundaLayers.scenarioRunnerLayer.mapError(TestFailure.fail))
    )

  }
  def spec: ZSpec[environment.TestEnvironment, Any] =
    suite("ScenarioRunnerSuite")(
      testM("run Scenario") {
        for {
          result <- scenarioRunner.run(ProcessScenario("myScenario", twitterProcess))
          _ = println(s"Scenario Result: $processInstance")
        } yield {
          assert(true)(equalTo(true) ?? "scenario Dummy")
        }
      }
    ).provideCustomLayer(CamundaLayers.scenarioRunnerLayer.mapError(TestFailure.fail))

}
