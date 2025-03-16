package camundala.gateway

import camundala.domain.InOutEncoder
import sttp.tapir.Schema.annotations.description

trait WorkerService:
  @description("Starts a worker synchronously")
  def startWorker[In <: Product : InOutEncoder](
      @description("Worker definition ID") workerDefId: String,
      @description("Input variables") in: In
  ): ProcessInfo

  @description("Registers a worker for a specific topic")
  def registerWorkers(
      @description("Topic name to subscribe to")
      workers: Seq[ProcessWorker]
  ): Unit
end WorkerService
