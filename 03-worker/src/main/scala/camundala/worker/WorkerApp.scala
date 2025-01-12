package camundala.worker

import zio.*

import scala.compiletime.uninitialized

trait WorkerApp extends ZIOAppDefault:
  def workerClients: Seq[WorkerClient[?]]
  var theWorkers: Set[JobWorker] = uninitialized

  def workers(dWorkers: (JobWorker | Seq[JobWorker])*): Unit =
    theWorkers = dWorkers
      .flatMap:
        case d: JobWorker => Seq(d)
        case s: Seq[?] => s.collect{case d: JobWorker => d}
      .toSet

  override def run: ZIO[Any, Any, Any] =
    for
      _ <- Console.printLine("Starting WorkerApp")
      _ <- ZIO.collectAllPar(workerClients.map(_.run(theWorkers)))
    yield ()   
