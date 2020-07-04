package pme123.camundala.camunda

import org.camunda.bpm.engine.{IdentityService, ProcessEngine, RuntimeService, TaskService}
import zio._

object camundaProcessEngine {
  type CamundaProcessEngine = Has[Service]

  trait Service {
    def runtimeService: UIO[RuntimeService]

    def taskService: UIO[TaskService]

    def identityService: UIO[IdentityService]
  }

  def runtimeService: URIO[CamundaProcessEngine,RuntimeService] =
    ZIO.accessM(_.get.runtimeService)

  def taskService: URIO[CamundaProcessEngine,TaskService] =
    ZIO.accessM(_.get.taskService)

  def identityService: URIO[CamundaProcessEngine,IdentityService]  =
    ZIO.accessM(_.get.identityService)

  type CamundaProcessEngineDeps = Has[() => ProcessEngine]

  lazy val live: ZLayer[CamundaProcessEngineDeps, Throwable, CamundaProcessEngine] =
    ZLayer.fromService[() => ProcessEngine, Service] {
      processEngine =>

        new Service {
          lazy val runtimeService: UIO[RuntimeService] =
            UIO(processEngine().getRuntimeService)

          lazy val taskService: UIO[TaskService] =
            UIO(processEngine().getTaskService)

          lazy val identityService: UIO[IdentityService] =
            UIO(processEngine().getIdentityService)
        }
    }

}
