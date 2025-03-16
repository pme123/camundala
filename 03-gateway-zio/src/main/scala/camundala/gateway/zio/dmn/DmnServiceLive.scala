package camundala.gateway.zio.dmn

import camundala.domain.*
import camundala.gateway.{ProcessInfo, DmnService}
import camundala.gateway.json.JsonDmnService
import io.circe.syntax.*
import zio.*

case class DmnServiceLive(jsonService: JsonDmnService) extends DmnService:
  def executeDmn[In <: Product: InOutEncoder](
      dmnDefId: String, 
      in: In
  ): ProcessInfo = 
    val jsonIn = in.asJson
    jsonService.executeDmn(dmnDefId, jsonIn)

object DmnServiceLive:
  val layer: URLayer[JsonDmnService, DmnService] = 
    ZLayer.fromFunction(DmnServiceLive(_))
