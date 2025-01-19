package camundala.worker

import zio.ZIO
import zio.ZIO.*

trait WorkerRegistry[T <: JobWorker]:
  def register(workers: Set[JobWorker]): ZIO[Any, Any, Any] =
    logInfo(s"Registering Workers for ${getClass.getSimpleName}") *>
      registerWorkers(workers.collect { case w: T => w })

  protected def registerWorkers(workers: Set[T]): ZIO[Any, Any, Any]
end WorkerRegistry
