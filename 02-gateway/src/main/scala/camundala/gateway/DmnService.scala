package camundala.gateway

import camundala.domain.*
import zio.*

trait DmnService:
  def executeDmn[In <: Product: InOutEncoder](
      dmnDefId: String,
      in: In
  ): IO[GatewayError, ProcessInfo]
end DmnService
