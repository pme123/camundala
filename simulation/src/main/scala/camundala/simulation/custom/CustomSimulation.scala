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
    extends
    SimulationDsl[LogLevel],
      DmnScenarioExtensions {

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
      .map { case level -> scenarioResults =>
        printResult(level, scenarioResults)
      }
      .head /*
      .foreach {
        case LogLevel.ERROR => //fail("There are Errors in the Simulation.")
        case LogLevel.WARN => //fail("There are Warnings in the Simulation.")
        case _ => succeeded
      }*/

  private def printResult(
      level: LogLevel,
      scenarioResults: Seq[ScenarioResult]
  ): LogLevel =
    val name = getClass.getSimpleName
    val line = "~" * ((80 - name.length) / 2)
    val maxLine = 85
    println(
      s"""${level.color}${s"$line START $name $line".takeRight(maxLine)}${Console.RESET}
         |${scenarioResults.map(_.log).mkString("\n")}
         |${"-" * maxLine}
         |${level.color}Scenarios with Level $level:${Console.RESET}
         |${scenarioResults.map { scenRes => s"- ${scenRes.name}" }.mkString("\n")}
         |${level.color}${s"$line END $name $line".takeRight(maxLine)}${Console.RESET}
         |""".stripMargin
    )
    level
}
