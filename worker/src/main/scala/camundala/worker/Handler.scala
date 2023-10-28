package camundala
package worker

import camundala.worker.CamundalaWorkerError.ValidatorError
import domain.*

sealed trait Handler 

case class ValidationHandler[
  In <: Product: CirceCodec,
](validate: In => Either[ValidatorError, In]):
  def handle(in: In): Either[ValidatorError, In] = validate(in)
end ValidationHandler
  

