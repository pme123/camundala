package camundala
package worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.*
import zio.*

final class WorkRunner[
    In <: Product: InOutCodec,
    Out <: Product: InOutCodec,
    T <: Worker[In, Out, ?]
](worker: T):

  def run(inputObject: In)(using EngineRunContext): IO[RunWorkError, Out | NoOutput] =
    worker.runWorkHandler
      .map:
        _.runWorkZIO(inputObject)
      .getOrElse:
        ZIO.succeed(NoOutput())

end WorkRunner
