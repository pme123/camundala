package camundala
package worker

import camundala.worker.CamundalaWorkerError.{InitProcessError, ValidatorError}
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
 *  or (with implicit conversion)
 *  ```
 *   .withValidation(
 *       (in: In) => Right(in)
 *   )
 *  ```
 * Default is no extra Validation.
  */
trait ValidationHandler[In <: Product : circe.Codec]:
  def validate(in: In): Either[ValidatorError, In]

object ValidationHandler:
  def apply[
    In <: Product : CirceCodec,
  ](funct: In => Either[ValidatorError, In]): ValidationHandler[In] =
    new ValidationHandler[In] {
      override def validate(in: In): Either[ValidatorError, In] =
        funct(in)
    }

/**
 * handler for Custom Process Initialisation.
 * All the variables in the Result Map will be put on the process.
 *
 *  For example if you want to init process Variables to a certain value.
 *
 *  Usage:
 *  ```
 *   .withValidation(
 *     InitProcessHandler(
 *       (in: In) => {
 *        Right(
 *          Map("isCompany" -> true)
 *        ) // success
 *       }
 *     )
 *   )
 *  ```
 *  or (with implicit conversion)
 *  ```
 *   .withValidation(
 *       (in: In) => {
 *        Right(
 *          Map("isCompany" -> true)
 *        ) // success
 *       }
 *   )
 *  ```
 * Default is no extra Validation.
 */
trait InitProcessHandler[
  In <: Product: CirceCodec,
]:
  def init( input:In): Either[InitProcessError, Map[String, Any]]

object InitProcessHandler:
  def apply[
    In <: Product : CirceCodec,
  ](funct: In => Either[InitProcessError, Map[String, Any]]): InitProcessHandler[In] =
    new InitProcessHandler[In] {
      override def init(in: In): Either[InitProcessError, Map[String, Any]] =
        funct(in)
    }  