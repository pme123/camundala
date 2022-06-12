package camundala.simulation

import camundala.bpmn.CamundaVariable.CInteger
import camundala.api.{CamundaProperty, CompleteTaskOut, CorrelateMessageIn, FormVariables, StartProcessIn}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.core.structure.*
import camundala.bpmn.*
import io.circe.parser.parse
import io.gatling.http.request.builder.HttpRequestBuilder
import io.circe.syntax.*

trait GatlingSimulation
  extends Simulation,
    SScenarioExtensions,
    SSubProcessExtensions,
    SUserTaskExtensions:

  private def httpProtocol: HttpProtocolBuilder =
    http
      .baseUrl(config.endpoint)
      .header("Content-Type", "application/json")

  def run(sim: SSimulation): Unit =
    setUp(toGatling(sim))
      .protocols(httpProtocol)
      .assertions(global.failedRequests.count.is(0))

  private def toGatling(sim: SSimulation): List[PopulationBuilder] =
    sim.scenarios.map(toGatling)

  private def toGatling(scen: SScenario): PopulationBuilder =
    def toGatling(step: SStep): Seq[ChainBuilder] = step match
      case ut: SUserTask =>
        ut.getAndComplete()
      case sp: SSubProcess =>
        sp.switchToSubProcess() ++
          sp.steps.flatMap(toGatling) ++
          sp.check() :+
          sp.switchToMainProcess()
    val testRequests = scen match
      case ps: ProcessScenario =>
        (scen.start() +:
          ps.steps.flatMap(toGatling)) ++
          scen.check()
      case bs: BadScenario =>
        Seq(scen.start(bs.status, bs.errorMsg))
    scenario(scen.name)
      .doIf(scen.isIgnored)(exec { session =>
        println(s">>> Scenario '${scen.name}' is ignored!")
        session
      })
      .doIf(!scen.isIgnored)(
        exec(config.preRequests.map(r => r()))
          .repeat(config.executionCount) {
            exec(
              testRequests
            )
          }
      )
      .inject(atOnceUsers(config.userAtOnce))

