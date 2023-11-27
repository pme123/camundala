package camundala
package camunda8

import domain.*
import cats.syntax.show.*
import io.circe
import io.circe.DecodingFailure
import org.springframework.web.bind.annotation.RestController
import io.circe.parser.decode

@RestController
trait Validator:

  def validate[T: JsonDecoder](json: String): Either[String, T] =
    decode[T](json) match
      case Left(error: DecodingFailure) =>
        Left(error.show)
      case Left(error: circe.Error) =>
        Left(error.show)
      case Right(p: T) =>
        Right(p)
