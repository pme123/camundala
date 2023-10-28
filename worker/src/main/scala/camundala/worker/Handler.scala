package camundala
package worker

import camundala.worker.CamundalaWorkerError.ValidatorError
import domain.*
import io.circe


/**
 * handler for Custom Validation
 *  (next to the automatic Validation of the In Object.
 *
 *  For example if one of two optional variables must exist.
 *
 *  Usage:
 *  ```
 *   .withValidation(
 *     ValidationHandler(
 *       (in: In) => Right(in)
 *     )
 *   )
 *  ```
 *
 * Default is no extra Validation.
  */
trait ValidationHandler[In <: Product : circe.Codec]:
  def validate(in: In): Either[ValidatorError, In]

object ValidationHandler:
  def apply[
    In <: Product : CirceCodec,
  ](validateFun: In => Either[ValidatorError, In]): ValidationHandler[In] =
    new ValidationHandler[In] {
      override def validate(in: In): Either[ValidatorError, In] =
        validateFun(in)
    }

trait InitProcessHandler[
  In <: Product: CirceCodec,
]: