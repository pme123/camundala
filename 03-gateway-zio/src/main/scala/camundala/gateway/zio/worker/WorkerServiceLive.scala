package camundala.gateway.zio.worker

import camundala.domain.InOutEncoder
import camundala.gateway.{ProcessInfo, ProcessWorker, WorkerService}
import zio.*

class WorkerServiceLive() extends WorkerService:

  def startWorker[In <: Product: InOutEncoder](
      workerDefId: String,
      in: In
  ): ProcessInfo = ???

  def registerWorkers(
      workers: Seq[ProcessWorker]
  ): Unit = ???
  
end WorkerServiceLive

object WorkerServiceLive:
  val layer: ULayer[WorkerService] =
    ZLayer.succeed(WorkerServiceLive())
