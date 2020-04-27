package pme123.camundala.model

import zio._
import zio.stm.TMap

/**
  * Registry to register a Bpmn.
  * Each BPMN must be registered in order to be managed by Camundala.
  */
object processRegister {

  type ProcessRegister = Has[Service]

  trait Service {

    def registerProcess(process: BpmnProcess): UIO[Unit]

    def unregisterProcess(id: String): UIO[Unit]

    def requestProcess(id: String): UIO[Option[BpmnProcess]]

  }

  def registerProcess(process: BpmnProcess): URIO[ProcessRegister, Unit] =
    ZIO.accessM(_.get.registerProcess(process))

  def unregisterProcess(id: String): URIO[ProcessRegister, Unit] =
    ZIO.accessM(_.get.unregisterProcess(id))

  def requestProcess(id: String): URIO[ProcessRegister, Option[BpmnProcess]] =
    ZIO.accessM(_.get.requestProcess(id))

  private lazy val bpmnIdMapSTM = TMap.make[String, BpmnProcess]()

  val live: ULayer[ProcessRegister] = ZLayer.fromEffect {
    bpmnIdMapSTM.commit.map { bpmnMap =>
      new Service {
        def registerProcess(process: BpmnProcess): UIO[Unit] =
          bpmnMap.put(process.id, process).commit *>
            bpmnMap.values.commit.map(v => println(s"registered: $v"))

        def unregisterProcess(id: String): UIO[Unit] =
          bpmnMap.delete(id).commit

        def requestProcess(id: String): UIO[Option[BpmnProcess]] =
          bpmnMap.values.commit.map(v => (println(s"request: $v"))) *>
            bpmnMap.get(id).commit

      }
    }
  }

}
