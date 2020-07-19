package pme123.camundala.camunda.delegate

import eu.timepit.refined.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution}
import org.camunda.spin.DataFormats.json
import org.camunda.spin.Spin
import org.camunda.spin.plugin.variable.value.JsonValue
import org.springframework.stereotype.Service
import pme123.camundala.camunda.service.restService
import pme123.camundala.camunda.service.restService.Response.{HandledError, NoContent, WithContent}
import pme123.camundala.camunda.service.restService._
import pme123.camundala.camunda.{CamundaLayers, JsonEnDecoders}
import pme123.camundala.model.bpmn._
import pme123.camundala.model.bpmn.ops._
import zio.Runtime.default.unsafeRun
import zio.{Cause, ZIO, logging}


/**
  * Provide a generic REST service
  */
@Service("restService")
class RestServiceDelegate
  extends CamundaDelegate
    with JsonEnDecoders {

  def execute(execution: DelegateExecution): Unit = {
    val requestRes = extractVariable(execution) // this can not be done in a ZIO fibre (Camunda execution must be in the main Thread)
    val response = unsafeRun(
      (for {
        (req, mappings) <- ZIO.fromEither(requestRes)
        response <- restService.call(req, mappings)
        _ <- logging.log.debug(s"Rest call ${req.host.url} successful:\n$response")
      } yield (req.responseVariable, response))
        .catchAll {
          cex: Throwable => logging.log.error(s"Error: $cex", Cause.fail(cex)) *> ZIO.never
        }
        .provideCustomLayer(CamundaLayers.logLayer("RestServiceDelegate") ++
          CamundaLayers.restServicetLayer)
    )
    // this can not be done in a ZIO fibre (Camunda execution must be in the main Thread)
    response match {
      case (_, NoContent) => ()
      case (variable, WithContent(_, body)) =>
        execution.setVariable(variable, Spin.S(body, json()))
      case (_, HandledError(status, body)) =>
        throw new BpmnError(s"restService-$status", s"Handled Error with:\nStatus: $status\nBody: $body")
    }
  }

  private def extractVariable(execution: DelegateExecution): Either[Throwable, (Request, Map[String, Option[String]])] = {
    for {
      json <- Option(execution.getVariableTyped[JsonValue]("request"))
        .map(Right(_))
        .getOrElse(Left(RestServiceException("There is no Variable 'request' in your Bag.")))
      request <- decode[Request](json.getValueSerialized)
    } yield (request,
      request.variableDefs.defs
        .map {
          case VariableDef(key, VariableType.Json, defaultValue) =>
            val json = execution.getVariableTyped[JsonValue](key)
            key.value -> (if (json == null) {
              println(s"The JSON Variable '$key' is not set!")
              None
            } else Option(json.getValue).map(_.toString).orElse(defaultValue))
          case VariableDef(key, VariableType.BusinessKey, _) =>
            (key.value -> Some(execution.getBusinessKey))
          case VariableDef(key, _, defaultValue) =>
            (key.value -> Option(execution.getVariable(key.value)).map(_.toString).orElse(defaultValue))
        }.toMap
    )
  }
}

object RestServiceDelegate
  extends JsonEnDecoders {

  case class RestServiceTempl(request: Request) {

    def asServiceTask(id: BpmnNodeId): ServiceTask =
      ServiceTask(id)
        //  .javaClass("pme123.camundala.camunda.delegate.RestServiceDelegate")
        .delegate("#{restService}")
        .inputJson("request", request.asJson.toString()) // mapping is done in the Service, request.variableDefs)
  }

  case class RestServiceException(msg: String) extends CamundalaException

}
