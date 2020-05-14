package pme123.camundala.model.deploy

import zio._
import zio.stm.TMap

/**
  * Registry to register a Bpmn.
  * Each BPMN must be registered in order to be managed by Camundala.
  */
object deployRegister {

  type DeployRegister = Has[Service]

  trait Service {

    def registerDeploy(bpmn: Deploy): UIO[Unit]

    def unregisterDeploy(id: DeployId): UIO[Unit]

    def requestDeploy(id: DeployId): UIO[Option[Deploy]]

  }

  def registerDeploy(deploy: Deploy): URIO[DeployRegister, Unit] =
    ZIO.accessM(_.get.registerDeploy(deploy))

  def unregisterDeploy(id: DeployId): URIO[DeployRegister, Unit] =
    ZIO.accessM(_.get.unregisterDeploy(id))

  def requestDeploy(id: DeployId): URIO[DeployRegister, Option[Deploy]] =
    ZIO.accessM(_.get.requestDeploy(id))

  private lazy val deployIdMapSTM = TMap.make[DeployId, Deploy]()

  val live: ULayer[DeployRegister] = ZLayer.fromEffect {
    deployIdMapSTM.commit.map { deployMap =>
      new Service {
        def registerDeploy(deploy: Deploy): UIO[Unit] =
          deployMap.put(deploy.id, deploy).commit

        def unregisterDeploy(id: DeployId): UIO[Unit] =
          deployMap.delete(id).commit

        def requestDeploy(id: DeployId): UIO[Option[Deploy]] =
            deployMap.get(id).commit

      }
    }
  }
}
