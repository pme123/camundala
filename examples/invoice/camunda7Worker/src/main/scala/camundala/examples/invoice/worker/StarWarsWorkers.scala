package camundala.examples.invoice.worker

import camundala.camunda7.worker.EngineWorkerDsl
import camundala.domain.*
import camundala.examples.invoice.StarWarsRestApi.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Uri

@Configuration
class StarWarsApiWorker extends EngineWorkerDsl, ServiceWorkerDsl[In, Out, ServiceIn, ServiceOut]:

  serviceGET(example)

  lazy val apiUri: Uri = uri"https://swapi.dev/api/people/{id}"


  override protected def defaultHeaders: Map[String, String] = Map("justForTestHeader" -> "it works!")

  override protected def outputMapper(
      out: RequestOutput[ServiceOut]
  ): Either[ServiceMappingError, Option[Out]] =
    Right(Some(Out(out.outputBody)))

  println("INITIALIZED: StarWarsApiWorker")
end StarWarsApiWorker
