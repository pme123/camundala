package camundala.camunda7.worker

import camundala.domain.*

/** Allows you to initialize variables of the process with default values
  */
trait Initializer[In <: Product: CirceCodec]:

  type InitializerOutput = HelperContext[Either[CamundalaWorkerError, Map[String, Any]]]

  protected def initialize(
      inputObject: In,
  ): InitializerOutput = Right(Map.empty)

end Initializer
