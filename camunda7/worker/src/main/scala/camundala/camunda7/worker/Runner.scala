package camundala.camunda7.worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError
import org.camunda.bpm.client.task.ExternalTask

/** Allows you to initialize variables of the process with default values
  */
trait Runner[In <: Product: CirceCodec, Out <: Product: CirceCodec] extends CamundaHelper:

  type RunnerOutput = HelperContext[Either[CamundalaWorkerError, Option[Out]]]

  protected def runWork(inputObject: In, optOutMock: Option[Out]) : RunnerOutput = 
    Right(None)

end Runner


