package pme123.camundala.model.register

import pme123.camundala.model.bpmn.{Bpmn, BpmnId}
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

    def unregisterBpmn(id: BpmnId): UIO[Unit]

    def requestBpmn(id: BpmnId): UIO[Option[Bpmn]]

  }

  def registerBpmn(bpmn: Bpmn): URIO[BpmnRegister, Unit] =
    ZIO.accessM(_.get.registerBpmn(bpmn))

  def unregisterBpmn(id: BpmnId): URIO[BpmnRegister, Unit] =
    ZIO.accessM(_.get.unregisterBpmn(id))

  def requestBpmn(id: BpmnId): URIO[BpmnRegister, Option[Bpmn]] =
    ZIO.accessM(_.get.requestBpmn(id))

  private lazy val bpmnIdMapSTM = TMap.make[BpmnId, Bpmn]()

  val live: ULayer[BpmnRegister] = ZLayer.fromEffect {
    bpmnIdMapSTM.commit.map { bpmnMap =>
      new Service {
        def registerBpmn(bpmn: Bpmn): UIO[Unit] =
          bpmnMap.put(bpmn.id, bpmn).commit

        def unregisterBpmn(id: BpmnId): UIO[Unit] =
          bpmnMap.delete(id).commit

        def requestBpmn(id: BpmnId): UIO[Option[Bpmn]] =
          bpmnMap.get(id).commit

      }
    }
  }
}
