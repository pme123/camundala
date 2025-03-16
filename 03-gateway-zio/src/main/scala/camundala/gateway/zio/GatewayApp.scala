package camundala.gateway.zio

import camundala.gateway.{DmnService, ProcessService, WorkerService}
import camundala.gateway.zio.bpmn.ProcessServiceLive
import camundala.gateway.zio.dmn.DmnServiceLive
import camundala.gateway.zio.worker.WorkerServiceLive
import zio.*

object GatewayApp extends ZIOAppDefault:
  def run = 
    (for
      process <- ZIO.service[ProcessService]
      dmn     <- ZIO.service[DmnService]
      worker  <- ZIO.service[WorkerService]
      // Example usage
      _ <- Console.printLine("Gateway Services Ready")
    yield ())
    .provide(
      ProcessServiceLive.layer,
      DmnServiceLive.layer,
      WorkerServiceLive.layer
    )