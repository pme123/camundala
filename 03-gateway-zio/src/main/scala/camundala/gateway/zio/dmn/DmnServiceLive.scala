package camundala.gateway.zio.dmn

import camundala.domain.*
import camundala.gateway.{DmnService, ProcessInfo}
import zio.*

case class DmnServiceLive() extends DmnService:
  
  def executeDmn[In <: Product : InOutEncoder](
      dmnDefId: String, 
      in: In
  ): ProcessInfo = ???

object DmnServiceLive:
  val layer: ULayer[DmnService] =
    ZLayer.derive[DmnServiceLive]
