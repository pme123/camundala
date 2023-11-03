package camundala.examples.invoice.worker

import camundala.domain.*
import camundala.examples.invoice.StarWarsRestApi
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext

@Configuration
class StarWarsApiWorker extends EngineWorkerDsl:
  import StarWarsRestApi.*

  lazy val worker =
    service(StarWarsRestApi.example)
      .runWork(requestHandler)

  lazy val requestHandler: ServiceHandler[In, Out, NoInput, ServiceOut] =
    ServiceHandler(
      httpMethod = Method.GET,
      apiUri = uri"https://swapi.dev/api/people/{id}",
      defaultHeaders = Map(
        "crazy-header" -> "just-to-test"
      ),
      outputMapper = outputMapper
    )
  private def outputMapper(
      out: RequestOutput[ServiceOut]
  ): Either[ServiceMappingError, Option[Out]] =
    Right(Some(Out(out.outputBody)))
  
end StarWarsApiWorker
