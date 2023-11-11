package camundala.examples.invoice.worker

import camundala.bpmn.ServiceTask
import camundala.domain.*
import camundala.examples.invoice.StarWarsRestApi.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy
import org.camunda.bpm.client.impl.ExternalTaskClientBuilderImpl
import org.camunda.bpm.client.topic.TopicSubscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Uri

import javax.annotation.PostConstruct
import camundala.examples.invoice.StarWarsRestApi.given
@Configuration
class StarWarsApiWorker extends InvoiceWorkerHandler, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]:

  lazy val serviceTask = example

  def apiUri(in: In): Uri = uri"https://swapi.dev/api/people/${in.id}"

  override def defaultHeaders: Map[String, String] = Map(
    "fromHeader" -> "okidoki"
  )

  override def outputMapper(
      out: ServiceResponse[ServiceOut]
  ): Either[ServiceMappingError, Option[Out]] =
    Right(Some(Out(out.outputBody, out.headers.getOrElse("fromHeader", "---"))))
  
end StarWarsApiWorker
