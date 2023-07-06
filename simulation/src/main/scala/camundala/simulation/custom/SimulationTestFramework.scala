package camundala.simulation
package custom

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

final class SimulationTestFramework extends sbt.testing.Framework:

  val name: String = "CSimulation"

  val fingerprints: Array[sbt.testing.Fingerprint] = Array(
    SimulationFingerprint
  )
  println(s"SimulationTestFramework started")
  def runner(
      args: Array[String],
      remoteArgs: Array[String],
      testClassLoader: ClassLoader
  ): SimulationRunner =
    println("TestRunner started")
    new SimulationRunner(args, remoteArgs, testClassLoader)

end SimulationTestFramework

object SimulationFingerprint extends sbt.testing.SubclassFingerprint:
  def superclassName(): String = "camundala.simulation.custom.CustomSimulation"
  final def isModule() = false
  final def requireNoArgConstructor() = true

final class SimulationRunner(
    val args: Array[String],
    val remoteArgs: Array[String],
    testClassLoader: ClassLoader
) extends sbt.testing.Runner:
  private val maxLine = 85

  override def tasks(
      taskDefs: Array[sbt.testing.TaskDef]
  ): Array[sbt.testing.Task] = {
    taskDefs.map { td =>
      Task(
        td,
        (loggers, eventHandler) =>
          Future {
            val startTime = System.currentTimeMillis()
            val futSimResults = Class
              .forName(td.fullyQualifiedName())
              .getDeclaredConstructor()
              .newInstance()
              .asInstanceOf[CustomSimulation]
              .simulation
            val simResults = Await.result(futSimResults, 5.minutes)
            val time = System.currentTimeMillis() - startTime
            val timeInSec = time / 1000
            val name = td.fullyQualifiedName().split('.').last
            val line = "~" * (((maxLine - 5) - name.length) / 2)
            val logLevel = simResults.head._1
            println(
              s"""${logLevel.color}${s"$line START $name $line"
                .takeRight(maxLine)}${Console.RESET}
                 |${simResults.reverse.flatMap((sr: (LogLevel, Seq[ScenarioResult])) => sr._2.map(_.log)).mkString("\n")}
                 |${simResults.map(sr => printResult(sr._1, sr._2)).mkString("\n")}
                 |${logLevel.color}${s"$line END $name in $timeInSec sec $line"
                .takeRight(maxLine)}${Console.RESET}
                 |""".stripMargin
            )

            eventHandler.synchronized {
              eventHandler.handle(new sbt.testing.Event {
                def fullyQualifiedName(): String = td.fullyQualifiedName()

                def throwable(): sbt.testing.OptionalThrowable =
                  sbt.testing.OptionalThrowable()

                def status(): sbt.testing.Status = logLevel match {
                  case LogLevel.ERROR =>
                    sbt.testing.Status.Failure
                  case _ =>
                    sbt.testing.Status.Success
                }

                def selector(): sbt.testing.NestedTestSelector = {
                  new sbt.testing.NestedTestSelector(
                    fullyQualifiedName(),
                    "Simulation"
                  )
                }

                def fingerprint(): sbt.testing.Fingerprint = td.fingerprint()

                def duration(): Long = time
              })
            }
          }
      )
    }
  }

  override def done(): String =
    "All Simulations done - see the console above for more information"

  private def printResult(
      level: LogLevel,
      scenarioResults: Seq[ScenarioResult]
  ): String =
    s"""${"-" * maxLine}
       |${level.color}Scenarios with Level $level:${Console.RESET}
       |${scenarioResults
      .map { scenRes => s"- ${scenRes.name}" }
      .mkString("\n")}""".stripMargin

end SimulationRunner

class Task(
    val taskDef: sbt.testing.TaskDef,
    runUTestTask: (
        Seq[sbt.testing.Logger],
        sbt.testing.EventHandler
    ) => Future[Unit]
) extends sbt.testing.Task {

  def tags(): Array[String] = Array()

  def execute(
      eventHandler: sbt.testing.EventHandler,
      loggers: Array[sbt.testing.Logger]
  ): Array[sbt.testing.Task] = {
    Await.ready(
      runUTestTask(loggers, eventHandler),
      5.minutes
    )
    Array()
  }
}
