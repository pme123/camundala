package camundala.examples.twitter.camunda

import camundala.bpmn.*
import camundala.examples.twitter.api.TwitterApi.Tweet
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent
import io.circe.syntax.EncoderOps
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.{PutMapping, RequestBody}

trait RestEndpoint extends Validator:

  type Response = ResponseEntity[ProcessInstanceEvent | String]

  @Autowired
  protected var zeebeClient: ZeebeClient = _

  def startProcess[T <: Product: Encoder: Decoder](
      processId: String,
      processVariables: T
  ): Response =
    (for
      _ <- validate(processVariables)
      process <- start(processId, processVariables)
    yield process) match
      case Right(process) =>
        ResponseEntity
          .status(HttpStatus.OK)
          .body(process)
      case Left(errorMsg) =>
        ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(errorMsg)

  private def start[T <: Product: Encoder: Decoder](
      processId: String,
      processVariables: T
  ): Either[String, ProcessInstanceEvent] =
    try {
      Right(
        zeebeClient.newCreateInstanceCommand
          .bpmnProcessId(processId)
          .latestVersion
          .variables(processVariables)
          .send
          .join
      )
    } catch {
      case ex: Throwable =>
        ex.printStackTrace()
        Left(s"Problem starting the Process: ${ex.getMessage}")
    }
