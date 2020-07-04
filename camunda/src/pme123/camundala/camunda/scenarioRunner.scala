package pme123.camundala.camunda

import pme123.camundala.camunda.camundaProcessEngine.CamundaProcessEngine
import pme123.camundala.model.bpmn._
import pme123.camundala.model.scenarios.ProcessScenario
import zio._
import zio.logging.Logging
import zio.random.Random

import scala.jdk.CollectionConverters._

/**
  * FIRST Experiments
  * - start Camunda
  * - deploy BPMN
  * - run Scenario
  */
object scenarioRunner {
  type ScenarioRunner = Has[Service]

  trait Service {

    def run(scenario: ProcessScenario): Task[ScenarioResult]
  }

  def run(scenario: ProcessScenario): RIO[ScenarioRunner, ScenarioResult] =
    ZIO.accessM(_.get.run(scenario))

  type ScenarioRunnerDeps = CamundaProcessEngine with Logging with Random

  lazy val live: RLayer[ScenarioRunnerDeps, ScenarioRunner] =
    ZLayer.fromServices[camundaProcessEngine.Service,Random.Service, logging.Logger[String], Service] {
      (processEngine, random, log) =>
        new Service {
          def run(scenario: ProcessScenario): Task[ScenarioResult] = {
            for {

              runtimeService <- processEngine.runtimeService
              businessKey <- random.nextIntBetween(1000, 10000).map(scenario.businessKey + _)
              processInstance <- UIO(runtimeService.startProcessInstanceByKey(scenario.process.id.value,
                businessKey,
                Map[String, AnyRef]("customer" -> "muller").asJava
              ))
              taskService <- processEngine.taskService
              taskQuery = taskService.createTaskQuery().processDefinitionKey(businessKey)
              // check and complete task "Assign Approver"
              _ = taskService.complete(taskQuery.singleResult().getId,
                Map[String, AnyRef]("hello" -> "muller").asJava
              )
              //deploy <- engine.getRepositoryService.createDeployment()

              _ <- log.info(s"engines: $processInstance")
            } yield ScenarioResult()
          }
        }
    }

  case class BpmnServiceException(msg: String)
    extends CamundalaException

  case class ScenarioResult()

}
