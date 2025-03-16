package camundala.gateway

import camundala.domain.InOutEncoder
import sttp.tapir.Schema.annotations.description

trait DmnService:
  @description("Executes a DMN decision table synchronously")
  def executeDmn[In <: Product : InOutEncoder](
      @description("DMN definition ID") dmnDefId: String, 
      @description("Input variables") in: In
  ): ProcessInfo