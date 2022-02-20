package camundala
package gatling

import camundala.api.*
import camundala.bpmn.*
import io.gatling.core.Predef.*
import io.gatling.core.structure.{ChainBuilder, PopulationBuilder}
import io.gatling.http.Predef.*

import scala.language.implicitConversions

case class ProcessScenario(
    scenarioName: String,
    config: SimulationConfig,
    requests: Seq[ChainBuilder]
) extends ProcessExtensions:
  import gatling.*

  implicit lazy val simulationConfig: SimulationConfig = config
  implicit lazy val tenantId: Option[String] = config.tenantId

  def start[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): ProcessScenario =
    println(s"Process started with Config: $config")
    copy(requests = requests :+ process.start(scenarioName))

  def check[
      In <: Product: Encoder: Decoder: Schema,
      Out <: Product: Encoder: Decoder: Schema
  ](process: Process[In, Out]): ProcessScenario =
    copy(requests = requests ++ process.check(scenarioName))

  def steps(reqs: (ChainBuilder | Seq[ChainBuilder])*): ProcessScenario =
    copy(requests = requests ++ ProcessScenario.flatten(reqs))

  def toGatling: PopulationBuilder =
    println(s"toGatlin222 ($scenarioName, ${requests.map(_.getClass)})")
    scenario(scenarioName)
      .exec(requests)
      .inject(atOnceUsers(config.userAtOnce))

object ProcessScenario:

  def apply(
      scenarioName: String,
      preRequests: Seq[ChainBuilder],
      requests: (ChainBuilder | Seq[ChainBuilder])*
  ): WithConfig[ProcessScenario] =
    println(s"scenarioName: $scenarioName")
    new ProcessScenario(
      scenarioName,
      summon[SimulationConfig],
      preRequests ++ flatten(requests)
    )

  private def flatten(
      requests: Seq[ChainBuilder | Seq[ChainBuilder]]
  ): Seq[ChainBuilder] =
    requests.flatMap {
      case seq: Seq[ChainBuilder] => seq
      case o: ChainBuilder => Seq(o)
    }
