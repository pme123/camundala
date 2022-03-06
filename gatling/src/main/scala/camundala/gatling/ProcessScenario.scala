package camundala
package gatling

import camundala.api.*
import camundala.bpmn.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, PopulationBuilder}
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.language.implicitConversions

case class ProcessScenario(
    scenarioName: String,
    config: SimulationConfig,
    requests: Seq[ChainBuilder],
    isIgnored: Boolean = false
) extends ProcessExtensions:
  import gatling.*

  implicit lazy val simulationConfig: SimulationConfig = config
  implicit lazy val tenantId: Option[String] = config.tenantId

  def start[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): ProcessScenario =
    copy(requests = requests :+ process.start(scenarioName))

  def check[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): ProcessScenario =
    copy(requests = requests ++ process.check(scenarioName))

  def steps(reqs: (ChainBuilder | Seq[ChainBuilder])*): ProcessScenario =
    copy(requests = requests ++ ProcessScenario.flatten(reqs))

  def ignored = copy(isIgnored = true)

  def toGatling: PopulationBuilder =
    scenario(scenarioName)
      .doIf(isIgnored)(exec { session =>
        println(s">>> Scenario '$scenarioName' is ignored!")
        session
      })
      .doIf(!isIgnored)(exec(requests))
      .inject(atOnceUsers(config.userAtOnce))

object ProcessScenario:

  def apply(
      scenarioName: String,
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): WithConfig[ProcessScenario] =
    println(s"<<< Scenario '$scenarioName' added.")
    new ProcessScenario(
      scenarioName,
      summon[SimulationConfig],
      summon[SimulationConfig].preRequests.map(_()) ++ flatten(requests)
    )

  def flatten(
      requests: Seq[ChainBuilder | Seq[ChainBuilder]]
  ): Seq[ChainBuilder] =
    requests.flatMap {
      case seq: Seq[ChainBuilder] => seq
      case o: ChainBuilder => Seq(o)
    }
