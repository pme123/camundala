package camundala.simulation.gatling

import camundala.bpmn.CamundaVariable.CInteger
import camundala.api.{CamundaProperty, CompleteTaskOut, CorrelateMessageIn, FormVariables, StartProcessIn}
import io.gatling.http.Predef.*
import io.gatling.http.protocol.HttpProtocolBuilder
import io.gatling.core.Predef.*
import io.gatling.core.{Predef => gatling}
import io.gatling.core.structure.*
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.parser.parse
import io.gatling.http.request.builder.HttpRequestBuilder
import io.circe.syntax.*

trait GatlingSimulation
    extends Simulation,
      SimulationDsl[Unit],
      SScenarioExtensions,
      SSubProcessExtensions,
      SUserTaskExtensions,
      SEventExtensions:

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
      case e: SReceiveMessageEvent =>
        e.correlate(config.tenantId)
      case e: SReceiveSignalEvent =>
        e.sendSignal()
      case sp: SSubProcess =>
        sp.switchToSubProcess() ++
          sp.steps.flatMap(toGatling) ++
          sp.check() :+
          sp.switchToMainProcess()
      case SWaitTime(seconds) =>
        Seq(exec().pause(seconds))

    val testRequests = scen match
      case ps: ProcessScenario =>
        (ps.start() +:
          ps.steps.flatMap(toGatling)) ++
          ps.check()
      case ds: DmnScenario =>
        Seq(ds.evaluate())
      case bs: BadScenario =>
        Seq(bs.start(bs.status, bs.errorMsg))
      case is: IncidentScenario =>
        is.start() +: checkIncident(is.incidentMsg)

    gatling.scenario(scen.name)
      .doIf(scen.isIgnored)(exec { session =>
        println(s">>> Scenario '${scen.name}' is ignored!")
        session
      })
      .doIf(!scen.isIgnored)(
        exec(config.preRequests.map(r  => exec(r())))
          .repeat(config.executionCount) {
            exec(
              testRequests
            )
          }
      )
      .inject(atOnceUsers(config.userAtOnce))
