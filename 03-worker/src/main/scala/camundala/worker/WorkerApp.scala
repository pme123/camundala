package camundala.worker

import zio.*
import zio.ZIO.*

import scala.compiletime.uninitialized

trait WorkerApp extends ZIOAppDefault:
  def workerRegistries: Seq[WorkerRegistry[?]]
  protected var theWorkers: Set[JobWorker] = uninitialized

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = ZioLogger.logger

  def workers(dWorkers: (JobWorker | Seq[JobWorker])*): Unit =
    theWorkers = dWorkers
      .flatMap:
        case d: JobWorker => Seq(d)
        case s: Seq[?]    => s.collect { case d: JobWorker => d }
      .toSet

  override def run: ZIO[Any, Any, Any] =
    for
      _ <- logInfo("Starting WorkerApp")
      _ <- collectAllPar(workerRegistries.map(_.register(theWorkers)))
    yield ()
end WorkerApp
