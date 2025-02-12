package camundala.worker

import zio.ZIO
import zio.ZIO.*

trait WorkerRegistry[T <: WorkerDsl[?, ?]]:
  def register(workers: Set[WorkerDsl[?, ?]]): ZIO[Any, Any, Any] =
    logInfo(s"Registering Workers for ${getClass.getSimpleName}") *>
      registerWorkers(workers.collect { case w: T => w })

  protected def registerWorkers(workers: Set[T]): ZIO[Any, Any, Any]
end WorkerRegistry
