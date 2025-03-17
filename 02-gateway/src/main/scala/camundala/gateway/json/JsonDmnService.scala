package camundala.gateway.json

import camundala.gateway.ProcessInfo
import io.circe.Json

trait JsonDmnService:
  def executeDmn(
      dmnDefId: String, 
      in: Json
  ): ProcessInfo