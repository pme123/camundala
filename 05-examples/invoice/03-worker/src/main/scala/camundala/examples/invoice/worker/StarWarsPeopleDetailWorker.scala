package camundala.examples.invoice.worker

import camundala.domain.*
import camundala.examples.invoice.bpmn.StarWarsPeopleDetail.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Uri

@Configuration
class StarWarsPeopleDetailWorker extends CompanyWorkerHandler,
      ServiceWorkerDsl[In, Out, NoInput, ServiceOut]:

  lazy val serviceTask = example

  def apiUri(in: In) = uri"https://swapi.dev/api/people/${in.id}"

  override def querySegments(in:In): Seq[QuerySegmentOrParam] =
    queryKeys("id", "optName") ++
      queryKeyValues("a" -> in.id, "b" -> true) ++
      queryValues(12, false, null)

  override def validate(in: In): Either[ValidatorError, In] =
    if in.id <= 0 then
      Left(ValidatorError("The search id for People must be > 0!"))
    else
      super.validate(in)

  override def inputHeaders(in: In): Map[String, String] =
    Map("test-db-id" -> in.id.toString)
    
  override def outputMapper(
      serviceOut: ServiceResponse[ServiceOut],
      in: In
  ): Either[ServiceMappingError, Out] =
    Right(Out.Success(serviceOut.outputBody, serviceOut.headers.getOrElse("fromHeader", "---")))

end StarWarsPeopleDetailWorker
