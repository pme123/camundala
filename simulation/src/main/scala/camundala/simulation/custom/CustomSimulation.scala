package camundala.simulation.custom

import camundala.api.StartProcessIn
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*

trait CustomSimulation extends App, ProcessScenarioExtensions {

  def run(sim: SSimulation): Unit =
    sim.scenarios
      .map {
        case scen: ProcessScenario => scen -> scen.run()
        case other => other -> Right(Seq(ScenarioData().warn(s"UNSUPPORTED: ${other.name}"))).asInstanceOf[ResultType]
      }
      .map {(scen: SScenario , resultData: ResultType) =>
          val data: ScenarioData = resultData.fold(
           d => d,
           d => d
          )
          data.logEntries.filter(config.logLevel).foreach(println)
          scen.name -> data.logEntries.maxLevel
      }
      .groupBy(_._2)
      .toSeq
      .sortBy(_._1)
      .reverse
      .foreach {
        case LogLevel.ERROR -> scenarios =>
          println("*" * 20)
          println("Simulation FAILED! The following Scenarios failed:")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
          println("Check the logs above.")
        case LogLevel.WARN -> scenarios =>
          println("-" * 20)
          println("Simulation has WARNINGS! Check the following Scenarios:")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
        case _ => // nothing to do
      }
}
