package camundala.gateway.zio.bpmn

import camundala.domain.*
import camundala.gateway.{GatewayError, ProcessInfo, ProcessService}
import camundala.gateway.json.JsonProcessService
import io.circe.syntax.*
import zio.*

case class ProcessServiceLive(jsonService: JsonProcessService) extends ProcessService:
  def startProcess[In <: Product: InOutEncoder, Out <: Product: InOutDecoder](
      processDefId: String, 
      in: In
  ): IO[GatewayError, Out] = 
    ZIO.attempt {
      val jsonIn = in.asJson
      val jsonOut = jsonService.startProcess(processDefId, jsonIn)
      jsonOut.as[Out]
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.ProcessError(s"Failed to start process: ${ex.getMessage}"))
    }.flatMap {
      case Left(err) => ZIO.fail(GatewayError.DecodingError(s"Failed to decode response: ${err.getMessage}"))
      case Right(value) => ZIO.succeed(value)
    }

  def startProcessAsync[In <: Product: InOutEncoder](
      processDefId: String, 
      in: In
  ): IO[GatewayError, ProcessInfo] = 
    ZIO.attempt {
      val jsonIn = in.asJson
      jsonService.startProcessAsync(processDefId, jsonIn)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.ProcessError(s"Failed to start process async: ${ex.getMessage}"))
    }

  def sendMessage[In <: Product: InOutEncoder](
      messageDefId: String, 
      in: In
  ): IO[GatewayError, ProcessInfo] = 
    ZIO.attempt {
      val jsonIn = in.asJson
      jsonService.sendMessage(messageDefId, jsonIn)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.ProcessError(s"Failed to send message: ${ex.getMessage}"))
    }

  def sendSignal[In <: Product: InOutEncoder](
      signalDefId: String, 
      in: In
  ): IO[GatewayError, ProcessInfo] = 
    ZIO.attempt {
      val jsonIn = in.asJson
      jsonService.sendSignal(signalDefId, jsonIn)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.ProcessError(s"Failed to send signal: ${ex.getMessage}"))
    }

object ProcessServiceLive:
  val layer: URLayer[JsonProcessService, ProcessService] = 
    ZLayer.fromFunction(ProcessServiceLive(_))