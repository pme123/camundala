package pme123.camundala.model.bpmn

import zio.stm.TMap
import zio._

/**
  * Registry to register a Bpmn.
  * Each BPMN must be registered in order to be managed by Camundala.
  */
object bpmnRegister {

  type BpmnRegister = Has[Service]

  trait Service {

    def registerBpmn(bpmn: Bpmn): UIO[Unit]

    def unregisterBpmn(id: String): UIO[Unit]

    def requestBpmn(id: String): UIO[Option[Bpmn]]

  }

  def registerBpmn(bpmn: Bpmn): URIO[BpmnRegister, Unit] =
    ZIO.accessM(_.get.registerBpmn(bpmn))

  def unregisterBpmn(id: String): URIO[BpmnRegister, Unit] =
    ZIO.accessM(_.get.unregisterBpmn(id))

  def requestBpmn(id: String): URIO[BpmnRegister, Option[Bpmn]] =
    ZIO.accessM(_.get.requestBpmn(id))

  private lazy val bpmnIdMapSTM = TMap.make[String, Bpmn]()

  val live: ULayer[BpmnRegister] = ZLayer.fromEffect {
    bpmnIdMapSTM.commit.map { bpmnMap =>
      new Service {
        def registerBpmn(bpmn: Bpmn): UIO[Unit] =
          bpmnMap.put(bpmn.id, bpmn).commit

        def unregisterBpmn(id: String): UIO[Unit] =
          bpmnMap.delete(id).commit

        def requestBpmn(id: String): UIO[Option[Bpmn]] =
          bpmnMap.get(id).commit

      }
    }
  }
}
