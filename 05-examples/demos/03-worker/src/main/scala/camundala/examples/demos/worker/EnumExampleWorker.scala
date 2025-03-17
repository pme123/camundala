package camundala.examples.demos.worker

import camundala.domain.*
import camundala.examples.demos.bpmn.EnumWorkerExample.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import org.springframework.context.annotation.Configuration
import sttp.client3.UriContext
import sttp.model.Uri

@SpringConfiguration
class EnumExampleWorker extends CompanyServiceWorkerDsl[In, Out, NoInput, ServiceOut]:

  lazy val serviceTask = example

  def apiUri(in: In) =
    in match
      case In.A(someValue, enumEx, maybeOut, _) =>
        uri"https://swapi.dev/api/people/$someValue"
      case In.B(otherValue, _) =>
        uri"https://swapi.dev/api/people/$otherValue"

  override def outputMapper(
      serviceOut: ServiceResponse[ServiceOut],
      in: In
  ): Either[ServiceMappingError, Out] =
    in match
      case In.A(someValue, enumEx, _, _) =>
        Right(Out.A(someValue, 12, enumEx))
      case In.B(otherValue, _) =>
        Right(Out.B(Some(otherValue)))

end EnumExampleWorker
