package camundala.worker

import scala.concurrent.duration.*

trait JobWorker:
  def topic: String
  protected def worker: Worker[?, ?, ?]

  def timeout: Duration = 10.seconds

  protected def errorHandled(error: CamundalaWorkerError, handledErrors: Seq[String]): Boolean =
    error.isMock || // if it is mocked, it is handled in the error, as it also could be a successful output
      handledErrors.contains(error.errorCode.toString) || handledErrors.map(
        _.toLowerCase
      ).contains("catchall")

  protected def regexMatchesAll(
      errorHandled: Boolean,
      error: CamundalaWorkerError,
      regexHandledErrors: Seq[String]
  ) =
    val errorMsg = error.errorMsg.replace("\n", "")
    errorHandled && regexHandledErrors.forall(regex =>
      errorMsg.matches(s".*$regex.*")
    )
  end regexMatchesAll

  protected def filteredOutput(
      outputVariables: Seq[String],
      allOutputs: Map[String, Any]
  ): Map[String, Any] =
    outputVariables match
      case filter if filter.isEmpty => allOutputs
      case filter                   =>
        allOutputs
          .filter:
            case k -> _ => filter.contains(k)

  end filteredOutput
end JobWorker
