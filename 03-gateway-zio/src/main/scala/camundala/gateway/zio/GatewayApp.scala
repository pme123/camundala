package camundala.gateway.zio

import camundala.gateway.zio.bpmn.ProcessServiceLive
import camundala.gateway.zio.dmn.DmnServiceLive
import camundala.gateway.{DmnService, ProcessService, WorkerService}
import camundala.gateway.zio.json.*
import camundala.gateway.zio.worker.WorkerServiceLive
import zio.*

object GatewayApp extends ZIOAppDefault:
  def run = 
    (for
      process <- ZIO.service[ProcessService]
      dmn <- ZIO.service[DmnService]
      worker <- ZIO.service[WorkerService]
      _ <- Console.printLine("Gateway Services Ready")
    yield ())
    .provide(
      // JSON service implementations
      JsonProcessServiceLive.layer,
      JsonDmnServiceLive.layer,
      JsonWorkerServiceLive.layer,
      // Typed service implementations that depend on JSON services
      ProcessServiceLive.layer,
      DmnServiceLive.layer,
      WorkerServiceLive.layer
    )