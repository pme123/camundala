package camundala.gateway.zio.dmn

import camundala.domain.*
import camundala.gateway.{GatewayError, ProcessInfo, DmnService}
import camundala.gateway.json.JsonDmnService
import io.circe.syntax.*
import zio.*

case class DmnServiceLive(jsonService: JsonDmnService) extends DmnService:
  def executeDmn[In <: Product: InOutEncoder](
      dmnDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo] =
    ZIO.attempt {
      val jsonIn = in.asJson
      jsonService.executeDmn(dmnDefId, jsonIn)
    }.catchAll { case ex: Throwable =>
      ZIO.fail(GatewayError.DmnError(s"Failed to execute DMN: ${ex.getMessage}"))
    }
end DmnServiceLive

object DmnServiceLive:
  val layer: URLayer[JsonDmnService, DmnService] =
    ZLayer.fromFunction(DmnServiceLive(_))
