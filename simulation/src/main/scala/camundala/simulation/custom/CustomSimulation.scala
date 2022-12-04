package camundala.simulation.custom

import camundala.api.StartProcessIn
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import sttp.client3.*

trait CustomSimulation extends App, SScenarioExtensions {

  def run(sim: SSimulation): Unit =
    sim.scenarios
      .map {
        case scen: ProcessScenario => scen -> scen.run()
        case scen: IncidentScenario => scen -> scen.run()
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
          println("*" * 60)
          println(s"${Console.RED}Scenarios that FAILED:${Console.RESET}")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
          println("Check the logs above.")
          println("*" * 60)
        case LogLevel.WARN -> scenarios =>
          println("-" * 60)
          println(s"${Console.MAGENTA}Scenarios with WARNINGS:${Console.RESET}")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
        case _ -> scenarios =>
          println("." * 60)
          println(s"${Console.GREEN}Successful Scenarios:${Console.RESET}")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
      }
}
