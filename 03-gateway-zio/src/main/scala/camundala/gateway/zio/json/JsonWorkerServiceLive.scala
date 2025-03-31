package camundala.gateway.zio.json

import camundala.gateway.{ProcessInfo, ProcessWorker}
import camundala.gateway.json.JsonWorkerService
import io.circe.Json
import zio.*

case class JsonWorkerServiceLive() extends JsonWorkerService:
  def startWorker(
      workerDefId: String,
      in: Json
  ): ProcessInfo = ???

  def registerWorkers(
      workers: Seq[ProcessWorker]
  ): Unit = ???
end JsonWorkerServiceLive

object JsonWorkerServiceLive:
  val layer: ULayer[JsonWorkerService] =
    ZLayer.succeed(JsonWorkerServiceLive())
