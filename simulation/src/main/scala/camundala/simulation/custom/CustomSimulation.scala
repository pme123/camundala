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

abstract class CustomSimulation
    extends //AnyFunSuite,
    SimulationDsl[LogLevel],
      SScenarioExtensions {

  def simulation: LogLevel

  def run(sim: SSimulation): LogLevel =
    sim.scenarios
      .map {
        case scen: ProcessScenario => scen -> scen.run()
        case scen: IncidentScenario => scen -> scen.run()
        case scen: DmnScenario => scen -> scen.run()
        case other =>
          other -> Right(ScenarioData().warn(s"UNSUPPORTED: ${other.name}"))
      }
      .map { (scen: SScenario, resultData: ResultType) =>
        val data: ScenarioData = resultData.fold(
          d => d,
          d => d
        )
        val log =
          data.logEntries.filter(config.logLevel).map(_.toString).mkString("\n")
        ScenarioResult(scen.name, data.logEntries.maxLevel, log)
      }
      .groupBy(_.maxLevel)
      .toSeq
      .sortBy(_._1)
      .map {
        case LogLevel.ERROR -> scenarioResults =>
          println(s"""${Console.RED}~~~~~~~~~~ ${getClass.getSimpleName} ~~~~~~~~~~${Console.RESET}
                     |${scenarioResults.map(_.log).mkString("\n")}
                     |${"*" * 60}
                     |${Console.RED}Scenarios that FAILED:${Console.RESET}
                     |${scenarioResults.map { case scenRes => s"- ${scenRes.name}" }.mkString("\n")}
                     |Check the logs above.
                     |${Console.RED}~~~~~~~~~ END ${getClass.getSimpleName} ~~~~~~~~~${Console.RESET}
                     |""".stripMargin)
          LogLevel.ERROR
        case LogLevel.WARN -> scenarioResults =>
          println(s"""${Console.MAGENTA}~~~~~~~~~~ ${getClass.getSimpleName} ~~~~~~~~~~${Console.RESET}
                     |${scenarioResults.map(_.log).mkString("\n")}
                     |${"-" * 60}
                     |${Console.MAGENTA}Scenarios with WARNINGS:${Console.RESET}
                     |${scenarioResults.map { scenRes => s"- ${scenRes.name}" }.mkString("\n")}
                     |${Console.MAGENTA}~~~~~~~~~~ END ${getClass.getSimpleName} ~~~~~~~~~~${Console.RESET}
                     |""".stripMargin)
          LogLevel.WARN
        case l -> scenarioResults =>
          println(s"""${Console.GREEN}~~~~~~~~~~ ${getClass.getSimpleName} ~~~~~~~~~~${Console.RESET}
                     |${scenarioResults.map(_.log).mkString("\n")}
                     |${"." * 60}
                     |${Console.GREEN}Successful Scenarios:${Console.RESET}
                     |${scenarioResults.map { scenRes => s"- ${scenRes.name}" }.mkString("\n")}
                     |${Console.GREEN}~~~~~~~~~~ END ${getClass.getSimpleName} ~~~~~~~~~~${Console.RESET}
                     |""".stripMargin)
          l
      }
      .head /*
      .foreach {
        case LogLevel.ERROR => //fail("There are Errors in the Simulation.")
        case LogLevel.WARN => //fail("There are Warnings in the Simulation.")
        case _ => succeeded
      }*/

}
