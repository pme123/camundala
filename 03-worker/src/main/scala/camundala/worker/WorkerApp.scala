package camundala.worker

import zio.*
import zio.ZIO.*

import scala.compiletime.uninitialized

trait WorkerApp extends ZIOAppDefault:
  // a list of registries for each worker implementation
  def workerRegistries: Seq[WorkerRegistry[?]]
  // list all the workers you want to register
  def workers(dWorkers: (WorkerDsl[?, ?] | Seq[WorkerDsl[?, ?]])*): Unit =
    theWorkers = dWorkers
      .flatMap:
        case d: WorkerDsl[?, ?] => Seq(d)
        case s: Seq[?]          => s.collect { case d: WorkerDsl[?, ?] => d }
      .toSet

  def dependencies(workerApps: WorkerApp*): Unit                     =
    theDependencies = workerApps

  protected var theWorkers: Set[WorkerDsl[?, ?]] = uninitialized
  protected var theDependencies: Seq[WorkerApp]  = uninitialized

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = ZioLogger.logger

  override def run: ZIO[Any, Any, Any] =
    for
      _ <- logInfo(s"Starting WorkerApp: ${getClass.getSimpleName}")
      _ <- ZIO.foreachPar((theDependencies :+ this))(app => registerWorkers(app))
    yield ()
//TODO optimize registration
  private def registerWorkers(app: WorkerApp): ZIO[Any, Any, Any] =
    for
      _ <- logInfo(s"Starting WorkerApp: ${app.getClass.getSimpleName}")
      _ <- collectAllPar(workerRegistries.map(_.register(app.theWorkers)))
    yield ()
end WorkerApp
