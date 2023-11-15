package camundala.examples.invoice.worker

import camundala.bpmn.ServiceTask
import camundala.domain.*
import camundala.examples.invoice.ServiceMethodDeleteApi.{*, given}
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl
import org.camunda.bpm.client.topic.TopicSubscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.*

import javax.annotation.PostConstruct

@Configuration
class ServiceMethodDeleteWorker extends InvoiceWorkerHandler, ServiceWorkerDsl[In, Out, NoInput, ServiceOut]:

  lazy val serviceTask = example

  override val method = Method.DELETE

  def apiUri(in: In): Uri = uri"https://JustSomeUrl.ch/${in.id}"

end ServiceMethodDeleteWorker
