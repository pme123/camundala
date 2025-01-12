package camundala.worker

import camundala.worker.JobWorker
import zio.ZIO

trait WorkerClient[T <: JobWorker]:
  def run(workers: Set[JobWorker]): ZIO[Any, Any, Any] =
    runWorkers(workers.collect { case w: T => w })
  protected def runWorkers(workers: Set[T]): ZIO[Any, Any, Any]

