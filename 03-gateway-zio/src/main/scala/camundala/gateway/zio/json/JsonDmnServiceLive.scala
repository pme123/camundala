package camundala.gateway.zio.json

import camundala.gateway.ProcessInfo
import camundala.gateway.json.JsonDmnService
import io.circe.Json
import zio.*

case class JsonDmnServiceLive() extends JsonDmnService:
  def executeDmn(
      dmnDefId: String,
      in: Json
  ): ProcessInfo = ???
end JsonDmnServiceLive

object JsonDmnServiceLive:
  val layer: ULayer[JsonDmnService] =
    ZLayer.succeed(JsonDmnServiceLive())
