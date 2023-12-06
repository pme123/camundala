package camundala
package camunda7.worker

import camundala.worker.*
import org.camunda.bpm.client.task.ExternalTask
import sttp.client3.{HttpClientSyncBackend, Identity, SttpBackend}

export sttp.model.{Method, Uri, QueryParams}
export org.camunda.bpm.client.spring.annotation.ExternalTaskSubscription
export org.springframework.context.annotation.Configuration

lazy val backend: SttpBackend[Identity, Any] = HttpClientSyncBackend()

type HelperContext[T] = ExternalTask ?=> T
