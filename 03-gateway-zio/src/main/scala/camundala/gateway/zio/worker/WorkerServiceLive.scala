package camundala.gateway.zio.worker

import camundala.domain.*
import camundala.gateway.{GatewayError, ProcessInfo, ProcessWorker, WorkerService}
import camundala.gateway.json.JsonWorkerService
import io.circe.syntax.*
import zio.*

case class WorkerServiceLive(jsonService: JsonWorkerService) extends WorkerService:
  def startWorker[In <: Product: InOutEncoder](
      workerDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo] =
    ZIO.attempt {
      val jsonIn = in.asJson
      jsonService.startWorker(workerDefId, jsonIn)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.WorkerError(s"Failed to start worker: ${ex.getMessage}"))
    }

  def registerWorkers(
      workers: Seq[ProcessWorker]
  ): IO[GatewayError, Unit] =
    ZIO.attempt {
      jsonService.registerWorkers(workers)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.WorkerError(s"Failed to register workers: ${ex.getMessage}"))
    }
end WorkerServiceLive

object WorkerServiceLive:
  val layer: URLayer[JsonWorkerService, WorkerService] =
    ZLayer.fromFunction(WorkerServiceLive(_))
