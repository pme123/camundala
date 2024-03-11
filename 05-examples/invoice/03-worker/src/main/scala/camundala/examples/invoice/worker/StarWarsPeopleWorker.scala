package camundala.examples.invoice.worker

import camundala.domain.*
import camundala.examples.invoice.bpmn.StarWarsPeople.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Uri

@Configuration
class StarWarsPeopleWorker extends CompanyServiceWorkerDsl[In, Out, NoInput, ServiceOut]:

  lazy val serviceTask = example

  def apiUri(in: In) = uri"https://swapi.dev/api/people"

  override def outputMapper(
      serviceOut: ServiceResponse[ServiceOut],
      in: In
  ): Either[ServiceMappingError, Out] =
    val people = serviceOut.outputBody.results
      .filter(_.height.toInt > in.heightMoreThanInCm.getOrElse(0))
    Right(Out.Success(people))

end StarWarsPeopleWorker
