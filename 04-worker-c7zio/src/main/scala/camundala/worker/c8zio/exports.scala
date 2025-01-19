package camundala.worker.c8zio

import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}

lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()
