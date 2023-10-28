package camundala.examples.invoice
package worker

import camundala.camunda7.worker.DefaultRestApiClient
import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import camundala.worker.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Method

@Configuration
class ProjectWorkers extends EngineWorkerDsl:
  workers(
    initProcess(ReviewInvoice.example)
      //.withValidation(ReviewInvoiceWorker.customValidator)
      .withValidation(ReviewInvoiceWorker.validate) // implicit conversion
      .withInitVariables(ReviewInvoiceWorker.initVariables),
    service(StarWarsRestApi.example)
      .withRequestHandler(StarWarsRestApiWorker.requestHandler)
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
    lazy val customValidator = ValidationHandler(validate)
    def validate(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      // Left(ValidatorError("bad val test"))
      Right(in)

    def initVariables(in: In): Either[InitializerError, Map[String, Any]] =
      println("Do some variable initialization...")
      Right(Map("justToTestInit" -> true))
    end initVariables

  end ReviewInvoiceWorker

  object ArchiveInvoiceWorker:
    import ArchiveInvoice.*

    lazy val defaultHeaders: Map[String, String] = Map(
      "crazy-header" -> "just-to-test"
    )

    def bodyOutputMapper(
        requestOut: RequestOutput[ServiceOut]
    ): Right[Nothing, Some[Out]] =
      println("Do some crazy output mapping...")
      Right(Some(Out(Some(requestOut.outputBody.nonEmpty))))

    def runWork(
        inputObject: In,
        optOutput: Option[Out]
    ): Either[ServiceUnexpectedError, Option[Out]] =
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

    lazy val requestHandler: RequestHandler[In, Out, ServiceIn, ServiceOut] =
      RequestHandler(
        httpMethod = Method.GET,
        apiUri = uri"https://swapi.dev/api/people/1",
        defaultHeaders = Map(
          "crazy-header" -> "just-to-test"
        ),
        sendRequest = DefaultRestApiClient.sendRequest,
        outputMapper = outputMapper
      )
    private def outputMapper(
        out: RequestOutput[ServiceOut]
    ): Either[MappingError, Option[Out]] =
      Right(Some(Out(out.outputBody)))

  end StarWarsRestApiWorker

end ProjectWorkers
