package camundala.gateway

import camundala.domain.*
import zio.*

trait WorkerService:
  def startWorker[In <: Product: InOutEncoder](
    workerDefId: String,
    in: In
  ): IO[GatewayError, ProcessInfo]

  def registerWorkers(workers: Seq[ProcessWorker]): IO[GatewayError, Unit]
end WorkerService
