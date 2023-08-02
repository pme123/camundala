package camundala
package camunda8

import bpmn.*
import domain.*
import camundala.bpmn.CamundaVariable.CJson
import com.fasterxml.jackson.databind.ObjectMapper
import io.circe.syntax.*
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.{
  ProcessInstanceEvent,
  ProcessInstanceResult
}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import io.circe.parser
import scala.jdk.CollectionConverters.*

trait RestEndpoint extends Validator:

  type Response =
    ResponseEntity[ProcessInstanceEvent | ProcessInstanceResult | String]

  @Autowired
  protected var zeebeClient: ZeebeClient = _

  def createInstance[
      In <: Product: Decoder: Encoder,
      Out <: Product: Decoder: Encoder
  ](
      processId: String,
      startVars: Either[String, CreateProcessInstanceIn[In, Out]]
  ): Response =
    (for
      startObj <- startVars
      process <- start(processId, startObj)
    yield process) match
      case Right(process: ProcessInstanceEvent) =>
        ResponseEntity
          .status(HttpStatus.OK)
          .body(process)
      case Right(process: ProcessInstanceResult) =>
        extractBody[Out](process)
      case Left(errorMsg) =>
        ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(errorMsg)

  private def start[In <: Product: Decoder: Encoder, Out <: Product: Decoder](
      processId: String,
      startObj: CreateProcessInstanceIn[In, Out]
  ): Either[String, ProcessInstanceEvent | ProcessInstanceResult] =
    try {
      val command =
        zeebeClient.newCreateInstanceCommand
          .bpmnProcessId(processId)
          .latestVersion
          .variables(
            CamundaVariable
              .toCamunda(startObj.variables)
              .map { case k -> v => k -> toJackson(v) }
              .asJava
          )
      val endCommand =
        if (startObj.fetchVariables.isEmpty) command
        else {
          val fetchedVariables = startObj.fetchVariables.get.getDeclaredFields
            .map(_.getName)
            .toList
            .asJava
          println(s"fetchedVariables: $fetchedVariables")
          command
            .withResult()
            .fetchVariables(fetchedVariables)
        }
      Right(endCommand.send.join)
    } catch {
      case ex: Throwable =>
        ex.printStackTrace()
        Left(s"Problem starting the Process: ${ex.getMessage}")
    }

  end start

  private def toJackson(camundaVariable: CamundaVariable): Any =
    camundaVariable match
      case CJson(value, _) =>
        val jacksonMapper = new ObjectMapper()
        jacksonMapper.readTree(value)
      case _ =>
        camundaVariable.value

  private def extractBody[Out <: Product: Decoder: Encoder](
      process: ProcessInstanceResult
  ): Response =
    // parsing will validate output
    parser.parse(process.getVariables).flatMap(_.as[Out]) match
      case Right(out) =>
        val result = CreateProcessInstanceOut(
          process.getProcessDefinitionKey,
          process.getBpmnProcessId,
          process.getVersion,
          process.getProcessDefinitionKey,
          out
        )
        ResponseEntity
          .status(HttpStatus.OK)
          .body(result.asJson.toString)
      case Left(errorMsg) =>
        ResponseEntity
          .status(HttpStatus.BAD_REQUEST)
          .body(s"${errorMsg.getMessage} in body:\n${process.getVariables}")

  end extractBody
