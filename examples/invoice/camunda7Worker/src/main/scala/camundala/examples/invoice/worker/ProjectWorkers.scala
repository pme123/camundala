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
    initProcess(InvoiceReceipt.example),
    initProcess(ReviewInvoice.example)
      //.withValidation(ReviewInvoiceWorker.customValidator)
      .validation(ReviewInvoiceWorker.validate) // implicit conversion
      .initProcess(ReviewInvoiceWorker.initVariables),
    service(StarWarsRestApi.example)
      .runWork(StarWarsRestApiWorker.requestHandler),
    custom(ArchiveInvoice.example)
      .runWork(ArchiveInvoiceWorker.runWork)
  )

  object ReviewInvoiceWorker:
    import ReviewInvoice.*
    lazy val customValidator = ValidationHandler(validate)
    def validate(in: In): Either[ValidatorError, In] =
      println("Do some custom validation...")
      // Left(ValidatorError("bad val test"))
      Right(in)

    def initVariables(in: In): Either[InitProcessError, Map[String, Any]] =
      println("Do some variable initialization...")
      Right(Map("justToTestInit" -> true))
    end initVariables

  end ReviewInvoiceWorker

  object ArchiveInvoiceWorker:
    import ArchiveInvoice.*

    lazy val defaultHeaders: Map[String, String] = Map(
      "crazy-header" -> "just-to-test"
    )

    def runWork(
        inputObject: In,
        optOutput: Option[Out]
    ): Either[CustomError, Option[Out]] =
      println("Do some crazy things running work...")
      inputObject.shouldFail match
        case Some(false) =>
          Right(Some(Out(Some(true))))
        case Some(true) =>
          Left(CustomError("Could not archive invoice..."))
        case _ =>
          Right(Some(Out(Some(false))))

  end ArchiveInvoiceWorker

  object StarWarsRestApiWorker:

    import StarWarsRestApi.*

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

  end StarWarsRestApiWorker

end ProjectWorkers
