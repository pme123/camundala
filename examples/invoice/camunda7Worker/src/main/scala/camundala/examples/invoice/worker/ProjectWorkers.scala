package camundala.examples.invoice
package worker

import camundala.domain.*
import camundala.camunda7.worker.DefaultRestApiClient
import camundala.worker.CamundalaWorkerError.{InitializerError, MappingError, ServiceUnexpectedError, ValidatorError}
import camundala.worker.{EngineWorkerDsl, RequestHandler, RequestOutput}
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Method

@Configuration
class ProjectWorkers extends EngineWorkerDsl:
  import ArchiveInvoice.given
  workers(
    process(ReviewInvoice.example)
      .withCustomValidator(ReviewInvoiceWorker.customValidator)
      .withInitVariables(ReviewInvoiceWorker.initVariables),
    service(StarWarsRestApi.example,
      StarWarsRestApiWorker.requestHandler
    )
    /*
    service(ArchiveInvoice.example,
      ArchiveInvoiceWorker.requestHandler
    )
      .withDefaultHeaders(ArchiveInvoiceWorker.defaultHeaders)
      .withBodyOutputMapper(ArchiveInvoiceWorker.bodyOutputMapper)
     // .withWorkRunner(ArchiveInvoiceWorker.runWork)
     */

  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    def customValidator(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      // Left(ValidatorError("bad val test"))
      Right(in)
    end customValidator

    def initVariables(in: In): Either[InitializerError, Map[String, Any]] =
      println("Do some variable initialization...")
      Right(Map("justToTestInit" -> true))
    end initVariables

  end ReviewInvoiceWorker

  object ArchiveInvoiceWorker:
    import ArchiveInvoice.*

    lazy val defaultHeaders: Map[String, String] = Map("crazy-header" -> "just-to-test")

    def bodyOutputMapper(requestOut: RequestOutput[ServiceOut]): Right[Nothing, Some[Out]] =
      println("Do some crazy output mapping...")
      Right(Some(Out(Some(requestOut.outputBody.nonEmpty))))

    def runWork(inputObject: In, optOutput: Option[Out]): Either[ServiceUnexpectedError, Option[Out]] =
      println("Do some crazy things running work...")
      optOutput match
        case Some(out) =>
          Right(Some(out))
        case None if inputObject.shouldFail.getOrElse(false) =>
          Left(ServiceUnexpectedError("Could not archive invoice..."))
        case _ =>
          Right(Some(Out()))

  end ArchiveInvoiceWorker

  object StarWarsRestApiWorker:

    import StarWarsRestApi.*

    lazy val requestHandler: RequestHandler[In, Out, ServiceIn, ServiceOut] = RequestHandler(
      httpMethod = Method.GET,
      apiUri = uri"https://swapi.dev/api/people/1",
      sendRequest = DefaultRestApiClient.sendRequest,
      outputMapper = outputMapper
    )
    private def outputMapper(out: RequestOutput [ServiceOut]): Either[MappingError, Option[Out]] =
      Right(Some(Out(out.outputBody)))

  end StarWarsRestApiWorker

end ProjectWorkers
