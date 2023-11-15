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

  override def validate(in: In): Either[ValidatorError, In] =
    if(in.id <= 0)
      Left(ValidatorError("The search id for People must be > 0!"))
    else
      super.validate(in)

  override def outputMapper(
                             serviceOut: ServiceResponse[ServiceOut]
  ): Either[ServiceMappingError, Out] =
    Right(Out.Success(serviceOut.outputBody, serviceOut.headers.getOrElse("fromHeader", "---")))

  override protected def inputHeaders(in: In): Map[String, String] =
    Map("test-db-id" -> in.id.toString)

end StarWarsApiWorker
