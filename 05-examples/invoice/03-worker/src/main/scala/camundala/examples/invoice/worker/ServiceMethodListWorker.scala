package camundala.examples.invoice.worker

import camundala.bpmn.ServiceTask
import camundala.domain.*
import camundala.examples.invoice.bpmn.ServiceMethodListApi.{*, given}
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceMethodListWorker extends InvoiceWorkerHandler,
      ServiceWorkerDsl[In, Out, NoInput, ServiceOut]:

  lazy val serviceTask = example

  override val method = Method.GET

  def apiUri(in: In): Uri = uri"https://JustSomeUrl.ch/${in.id}"

  override def outputMapper(serviceOut: ServiceResponse[ServiceOut], in: In): Either[ServiceMappingError, Out] = 
    Right(Out(dummies = serviceOut.outputBody))
end ServiceMethodListWorker
