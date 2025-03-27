package camundala.worker

import zio.ZIO
import zio.ZIO.*

trait WorkerRegistry:
  final def register(workers: Set[WorkerDsl[?, ?]]): ZIO[Any, Any, Any] =
    logInfo(s"Registering Workers for ${getClass.getSimpleName}") *>
      registerWorkers(workers)

  protected def registerWorkers(workers: Set[WorkerDsl[?, ?]]): ZIO[Any, Any, Any]
end WorkerRegistry
