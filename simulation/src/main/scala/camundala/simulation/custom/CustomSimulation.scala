package camundala.simulation.custom

import camundala.api.StartProcessIn
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalatest.FutureOutcome.succeeded
import org.scalatest.funsuite.AnyFunSuite
import sttp.client3.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

abstract class CustomSimulation
    extends SimulationDsl[Future[Seq[(LogLevel, Seq[ScenarioResult])]]],
      DmnScenarioExtensions {

  def simulation: Future[Seq[(LogLevel, Seq[ScenarioResult])]]

  def run(sim: SSimulation): Future[Seq[(LogLevel, Seq[ScenarioResult])]] =
    Future
      .sequence(
        sim.scenarios
          .map {
            case scen: ProcessScenario => scen.run()
            case scen: IncidentScenario => scen.run()
            case scen: DmnScenario => scen.run()
            case scen: BadScenario => scen.run()
          }
      )
      .map(
        _.map { (resultData: ResultType) =>
          val data: ScenarioData = resultData.fold(
            d => d,
            d => d
          )
          val log =
            data.logEntries
              .filter(config.logLevel)
              .map(_.toString)
              .mkString("\n")
          ScenarioResult(data.scenarioName, data.logEntries.maxLevel, log)
        }
          .groupBy(_.maxLevel)
          .toSeq
          .sortBy(_._1)
      )
      .recover { ex =>
        {
          ex.printStackTrace()
          throw ex
        }
      }

}
