package camundala
package camunda8

import domain.*
import cats.syntax.show.*
import io.circe
import io.circe.DecodingFailure
import io.circe.parser.decode
import org.springframework.web.bind.annotation.RestController

@RestController
trait Validator:

  def validate[T: Decoder](json: String): Either[String, T] =
    decode[T](json) match
      case Left(error: DecodingFailure) =>
        Left(error.show)
      case Left(error: circe.Error) =>
        Left(error.show)
      case Right(p: T) =>
        Right(p)
