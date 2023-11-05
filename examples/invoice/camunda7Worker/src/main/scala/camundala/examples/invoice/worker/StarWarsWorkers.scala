package camundala.examples.invoice.worker

import camundala.camunda7.worker.EngineWorkerDsl
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

@Configuration
//@WorkerSubscription(example.topicDefinition)
class StarWarsApiWorker extends EngineWorkerDsl, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]:

  serviceGET(example)

  lazy val apiUri: Uri = uri"https://swapi.dev/api/people/{id}"

  override protected def defaultHeaders: Map[String, String] = Map(
    "justForTestHeader" -> "it works!"
  )

  override protected def outputMapper(
      out: RequestOutput[ServiceOut]
  ): Either[ServiceMappingError, Option[Out]] =
    Right(Some(Out(out.outputBody)))

  println("INITIALIZED: StarWarsApiWorker")

  private val BACKOFF_INIT_TIME = 500L
  private val BACKOFF_LOCK_FACTOR = 2L
  private val BACKOFF_LOCK_MAX_TIME = 600L
  private val WORKER_LOCK_DURATION = 2000L
  private val PROCESS_ENGINE_REST_URL = "http://localhost:8034/engine-rest/"

  private lazy val backoffStrategy =
     new ExponentialBackoffStrategy(BACKOFF_INIT_TIME, BACKOFF_LOCK_FACTOR, BACKOFF_LOCK_MAX_TIME)

  private lazy val externalTaskClient: ExternalTaskClient =
    new ExternalTaskClientBuilderImpl()
      .baseUrl(PROCESS_ENGINE_REST_URL)
      .backoffStrategy(backoffStrategy)
      .lockDuration(WORKER_LOCK_DURATION)
      .workerId(s"$topic-worker")
      .build

  @PostConstruct
  def registerHandler(): Unit =
    externalTaskClient
      .subscribe(topic)
      .handler(this)
      .open()
    println(s"registerHandler: $engineContext")
  end registerHandler

end StarWarsApiWorker
