package camundala.examples.invoice
package workers

import camundala.camunda7.worker.*
import camundala.camunda7.worker.CamundalaWorkerError.*
import camundala.domain.*
import camundala.examples.invoice.ArchiveInvoice.*
import sttp.client3.*

@Configuration
@ExternalTaskSubscription(value = serviceName)
class ArchiveInvoiceWorker extends ServiceWorker[In, Out, ServiceIn, ServiceOut]:

  lazy val defaultServiceMock = serviceMock

  override protected def runWork(inputObject: In, optOutput: Option[Out]): RunnerOutput =
    optOutput match
      case Some(out) =>
        Right(Some(out))
      case None if inputObject.shouldFail =>
        Left(ServiceUnexpectedError("Could not archive invoice..."))
      case _ =>
        Right(Some(Out()))

  protected def httpMethod = Method.GET

  protected def apiUri(inputObject: In): ApiUriType = Right(uri"http://myArchiveService.com")

  protected def sendRequest(request: Request[Either[String, String], Any], optReqBody: Option[ServiceIn]): Either[ServiceError, RequestOutput[ServiceOut]] = ???

  protected def prototype: In = example.in
end ArchiveInvoiceWorker
