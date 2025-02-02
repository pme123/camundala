package camundala.worker.c7zio

import org.camunda.bpm.client.task.ExternalTask
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}

lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

type HelperContext[T] = ExternalTask ?=> T
