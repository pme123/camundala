package camundala.worker.c7zio

import camundala.worker.WorkerRegistry
import org.camunda.bpm.client.ExternalTaskClient
import zio.ZIO.*
import zio.{Console, *}

class C7WorkerRegistry(client: C7Client)
    extends WorkerRegistry[C7Worker[?, ?]]:

  def registerWorkers(workers: Set[C7Worker[?, ?]]): ZIO[Any, Any, Any] =
    Console.printLine(s"Starting C7 Worker Client") *>
      acquireReleaseWith(client.client)(_.closeClient()): client =>
        for
          server <- ZIO.never.forever.fork
          _      <- collectAllPar(workers.map(w => registerWorker(w, client)))
          _      <- server.join
        yield ()

  private def registerWorker(worker: C7Worker[?, ?], client: ExternalTaskClient) =
    attempt(client
      .subscribe(worker.topic)
      .handler(worker)
      // .lockDuration(worker.timeout.toMillis)
      .open()) *>
      logInfo("Registered C7 Worker: " + worker.topic)

  extension (client: ExternalTaskClient)
    def closeClient() =
      logInfo("Closing C7 Worker Client") *>
        succeed(if client != null then client.stop() else ())
end C7WorkerRegistry
