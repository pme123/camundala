package camundala.worker.c8zio

import camundala.worker.{JobWorker, WorkerRegistry}
import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.impl.oauth.OAuthCredentialsProviderBuilder
import zio.ZIO.*
import zio.{Console, *}

import java.net.URI

class C8WorkerRegistry(client: C8Client)
    extends WorkerRegistry[C8Worker[?, ?]]:

  def registerWorkers(workers: Set[C8Worker[?, ?]]): ZIO[Any, Any, Any] =
    Console.printLine(s"Starting C8 Worker Client") *>
      acquireReleaseWith(client.client)(_.closeClient()): client =>
        for
          server <- attempt(
                      client
                        .newTopologyRequest
                        .send
                        .join
                    ).forever.fork
          _      <- collectAllPar(workers.map(w => registerWorker(w, client)))
          _      <- server.join
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
      succeed(if client != null then client.close() else ())

end C8WorkerRegistry
