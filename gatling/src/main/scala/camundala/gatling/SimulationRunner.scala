package camundala
package gatling

import camundala.api.*
import camundala.api.CamundaVariable.*
import camundala.bpmn
import camundala.bpmn.*
import camundala.domain.*
import io.circe.parser.*
import io.circe.syntax.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*

trait SimulationRunner
    extends Simulation,
      ProcessExtensions,
      CallActivityExtensions,
      UserTaskExtensions,
      EventExtensions:

  implicit def config: SimulationConfig = SimulationConfig()

  def httpProtocol: HttpProtocolBuilder =
    http
      .baseUrl(config.endpoint)
      .header("Content-Type", "application/json")

  inline def processScenario[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](inline process: Process[In, Out])(
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): ProcessScenario =
    processScenario(nameOfVariable(process))(
      process,
      requests: _*
    )

  def processScenario[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](scenarioName: String)(
      process: Process[In, Out],
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): ProcessScenario =
    ProcessScenario
      .apply(scenarioName)
      .start(process)
      .steps(requests: _*)
      .check(process)

  def processScenario(scenarioName: String)(
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): ProcessScenario =
    ProcessScenario.apply(scenarioName, requests: _*)

  inline def subProcess[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](inline callActivity: CallActivity[In, Out])(
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): Seq[ChainBuilder] = {
    val name = nameOfVariable(callActivity)
    Seq(callActivity.switchToSubProcess(name)) ++
      ProcessScenario.flatten(requests) ++
      callActivity.asProcess.check(name) :+
      callActivity.switchToMainProcess()
  }

  def simulate[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      example: Process[In, Out]
  ): Unit =
    simulate("example" -> example)

  def simulate[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      examples: (String, Process[In, Out])*
  ): Unit =
    simulate(examples.toMap)

  def simulate[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](
      examples: Map[String, Process[In, Out]]
  ): Unit =
    simulate(
      examples.toSeq.map { case (k, v) =>
        processScenario(k)(
          v
        )
      }: _*
    )

  def simulate(processScenarios: ProcessScenario*): Unit =
    setUp(processScenarios.map(_.toGatling): _*)
      .protocols(httpProtocol)
      .assertions(global.failedRequests.count.is(0))

end SimulationRunner
