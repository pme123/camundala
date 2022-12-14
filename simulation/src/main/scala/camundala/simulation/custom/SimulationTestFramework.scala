package camundala.simulation
package custom

import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

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

  override def tasks(
      taskDefs: Array[sbt.testing.TaskDef]
  ): Array[sbt.testing.Task] = {
    taskDefs.map { td =>
      Task(
        td,
        (loggers, eventHandler) =>
          Future {
            val startTime = System.currentTimeMillis()
            val logLevel = Class
              .forName(td.fullyQualifiedName())
              .getDeclaredConstructor()
              .newInstance()
              .asInstanceOf[CustomSimulation]
              .simulation
            val time = System.currentTimeMillis() - startTime
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

end SimulationRunner

import scala.concurrent.duration.*
import scala.concurrent.{Await, Future}

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
      100.seconds
    )
    Array()
  }
}
