package pme123.camundala.camunda.delegate

import eu.timepit.refined.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution}
import org.camunda.spin.DataFormats._
import org.camunda.spin.Spin
import org.camunda.spin.plugin.variable.value.JsonValue
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import pme123.camundala.camunda.service.restService
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.Response.{HandledError, NoContent, WithContent}
import pme123.camundala.camunda.service.restService._
import pme123.camundala.camunda.{CamundaLayers, JsonEnDecoders}
import pme123.camundala.model.ModelLayers
import pme123.camundala.model.bpmn.ConditionExpression.JsonExpression
import pme123.camundala.model.bpmn.Extensions.PropInOutExtensions
import pme123.camundala.model.bpmn.TaskImplementation.DelegateExpression
import pme123.camundala.model.bpmn._
import pme123.camundala.model.deploy.Sensitive
import zio.Runtime.default.unsafeRun
import zio.{ZIO, logging}


/**
  * Provide a generic REST service
  */
@Service("restService")
class RestServiceDelegate @Autowired()(runtimeService: RuntimeService)
  extends CamundaDelegate
    with JsonEnDecoders {

  def execute(execution: DelegateExecution): Unit = {
    val requestRes = extractVariables(execution)
    val response = unsafeRun(
      (for {
        request <- ZIO.fromEither(requestRes)
        response <- restService.call(request)
        _ <- logging.log.debug(s"Rest call ${request.host.url} successful:\n$response")
      } yield (request.responseVariable, response))
        .provideCustomLayer(ModelLayers.logLayer("RestServiceDelegate") ++
          CamundaLayers.restServicetLayer)
    )
    response match {
      case (_, NoContent) => ()
      case (variable, WithContent(_, body)) =>
        execution.setVariable(variable, Spin.S(body, json()))
      case (_, HandledError(status, body)) =>
        throw new BpmnError(s"restService-$status", s"Handled Error with:\nStatus: $status\nBody: $body")
    }
  }

  def extractVariables(execution: DelegateExecution) = {
    val json = execution.getVariableTyped[JsonValue]("request")
    val request = decode[Request](json.getValueSerialized)
      .map(request =>
        request.copy(
          mappings = request.mappings
            .keys
            .map { k =>
              k ->
                Option(execution.getVariable(k)).map(_.toString)
                  .getOrElse(request.mappings(k))
            }
            .toMap
        ))
    request
  }
}

object RestServiceDelegate
  extends JsonEnDecoders {
  val expression: DelegateExpression = DelegateExpression("#{restService}")
  val propPrefix = "rest-service"

  case class RestServiceTempl(request: Request) {

    def asServiceTask(id: BpmnNodeId): ServiceTask =
      ServiceTask(id,
        expression,
        PropInOutExtensions(
          inOuts = InputOutputs(
            inputs = List(
              InputOutput("request", JsonExpression(request.asJson.toString())),
            )
          )
        )
      )
  }

}
