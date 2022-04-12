package camundala.examples.twitter.camunda

import camundala.bpmn.*
import camundala.examples.twitter.api.TwitterApi.Tweet
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent
import io.circe.syntax.EncoderOps
import org.springframework.web.bind.annotation.RestController

@RestController
trait Validator:

  def validate[T <: Product: Encoder: Decoder](product: T): Either[String, T] =
    product.asJson.as[T] match
      case Right(p: T) =>
        Right(p)
      case Left(ex) =>
        Left(s"Validation Error: Input is not valid: $ex")
