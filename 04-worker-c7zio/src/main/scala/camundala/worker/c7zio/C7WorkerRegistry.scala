package camundala.worker.c7zio

import camundala.worker.{WorkerDsl, WorkerRegistry}
import org.camunda.bpm.client.ExternalTaskClient
import zio.ZIO.*
import zio.{Console, *}

class C7WorkerRegistry(client: C7Client)
    extends WorkerRegistry:

  protected def registerWorkers(workers: Set[WorkerDsl[?, ?]]): ZIO[Any, Any, Any] =
    acquireReleaseWith(client.client)(_.closeClient()): client =>
      for
        _                             <- ZIO.logInfo("Starting C7 Worker Client")
        server                        <- never.forever.fork
        c7Workers: Set[C7Worker[?, ?]] = workers.collect { case w: C7Worker[?, ?] => w }
        _                             <- foreachParDiscard(c7Workers)(w => registerWorker(w, client))
        _                             <- ZIO.logInfo(s"C7 Worker Client started - registered ${workers.size} workers")
        _                             <- server.join
      yield ()

  private def registerWorker(worker: C7Worker[?, ?], client: ExternalTaskClient) =
    attempt(client
      .subscribe(worker.topic)
      .handler(worker)
      .open()) *>
      logInfo("Registered C7 Worker: " + worker.topic)

  extension (client: ExternalTaskClient)
    def closeClient(): ZIO[Any, Nothing, Unit] =
      logInfo("Closing C7 Worker Client")
        .as(if client != null then client.stop() else ())
end C7WorkerRegistry
