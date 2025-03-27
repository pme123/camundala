package camundala.worker.c7zio

import camundala.worker.{WorkerDsl, WorkerRegistry}
import io.camunda.zeebe.client.ZeebeClient
import zio.ZIO.*
import zio.{Console, *}

class C8WorkerRegistry(client: C8Client)
    extends WorkerRegistry:

  protected def registerWorkers(workers: Set[WorkerDsl[?, ?]]): ZIO[Any, Any, Any] =
    Console.printLine(s"Starting C8 Worker Client") *>
      acquireReleaseWith(client.client)(_.closeClient()): client =>
        for
          server   <- ZIO.never.forever.fork
          c8Workers = workers.collect { case w: C8Worker[?, ?] => w }
          _        <- collectAllPar(c8Workers.map(w => registerWorker(w, client)))
          _        <- server.join
        yield ()

  private def registerWorker(worker: C8Worker[?, ?], client: ZeebeClient) =
    attempt(client
      .newWorker()
      .jobType(worker.topic)
      .handler(worker)
      .timeout(worker.timeout.toMillis)
      .open()) *>
      logInfo("Registered C8 Worker: " + worker.topic)

  extension (client: ZeebeClient)
    def closeClient() =
      logInfo("Closing C7 Worker Client") *>
        succeed(if client != null then client.close() else ())

end C8WorkerRegistry
