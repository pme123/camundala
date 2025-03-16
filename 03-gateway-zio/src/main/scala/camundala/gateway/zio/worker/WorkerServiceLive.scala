package camundala.gateway.zio.worker

import camundala.domain.*
import camundala.gateway.{ProcessInfo, ProcessWorker, WorkerService}
import camundala.gateway.json.JsonWorkerService
import io.circe.syntax.*
import zio.*

case class WorkerServiceLive(jsonService: JsonWorkerService) extends WorkerService:
  def startWorker[In <: Product: InOutEncoder](
      workerDefId: String, 
      in: In
  ): ProcessInfo = 
    val jsonIn = in.asJson
    jsonService.startWorker(workerDefId, jsonIn)

  def registerWorkers(
      workers: Seq[ProcessWorker]
  ): Unit = 
    jsonService.registerWorkers(workers)

object WorkerServiceLive:
  val layer: URLayer[JsonWorkerService, WorkerService] = 
    ZLayer.fromFunction(WorkerServiceLive(_))
