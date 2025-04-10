package camundala.gateway.json

import camundala.gateway.{ProcessInfo, ProcessWorker}
import io.circe.Json

trait JsonWorkerService:
  def startWorker(
      workerDefId: String,
      in: Json
  ): ProcessInfo

  def registerWorkers(
      workers: Seq[ProcessWorker]
  ): Unit
end JsonWorkerService
