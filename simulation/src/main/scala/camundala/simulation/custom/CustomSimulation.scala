package camundala.simulation.custom

import camundala.simulation.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


abstract class CustomSimulation
    extends SimulationDsl[Future[Seq[(LogLevel, Seq[ScenarioResult])]]],
      DmnScenarioExtensions {

  // needed that it can be called from the Test Framework and check the result
  var simulation: Future[Seq[(LogLevel, Seq[ScenarioResult])]] = _

  def run(sim: SSimulation): Future[Seq[(LogLevel, Seq[ScenarioResult])]] =
    simulation = Future
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
    simulation
  end run
}
