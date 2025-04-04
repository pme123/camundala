package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import io.circe.Json
import zio.*

final class OutMocker[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
](worker: T)(using context: EngineRunContext):

  def mockedOutput(in: In): IO[MockerError, Option[Out]] =
    (
      context.generalVariables.isMockedWorker(worker.topic),
      context.generalVariables.outputMock,
      context.generalVariables.outputServiceMock
    ) match
      // if the outputMock is set than we mock
      case (_, Some(outputMock), _) =>
        decodeMock(outputMock)
      // if your worker is mocked we use the default mock
      case (true, None, None)       =>
        worker.defaultMock(in).map(Some(_))
      // otherwise it is not mocked or it is a service mock which is handled in service Worker during running
      case (_, None, _)             =>
        ZIO.none
  end mockedOutput

  private def decodeMock(
      json: Json
  ) =
    ZIO.fromEither(json.as[Out])
      .map:
        Some(_)
      .mapError: error =>
        MockerError(errorMsg = s"$error:\n- $json")
  end decodeMock

end OutMocker