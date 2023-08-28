package camundala.camunda7.worker

import camundala.bpmn.*
import camundala.camunda7.worker.CamundalaWorkerError.*
import camundala.domain.*
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*

abstract class CamundalaWorker[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
] extends Validator[In],
      Initializer[In],
      Mocker[Out],
      Runner[In, Out],
      ExternalTaskHandler:
  worker =>

  override def execute(
      externalTask: ExternalTask,
      externalTaskService: ExternalTaskService
  ): Unit =
    println(
      s"WORKER ${LocalDateTime.now()} ${externalTask.getTopicName} (${externalTask.getId}) started"
    )
    executeWorker(externalTaskService)(using externalTask)
    println(
      s"WORKER ${LocalDateTime.now()} ${externalTask.getTopicName} ended"
    )
  end execute

  private def executeWorker(
      externalTaskService: ExternalTaskService
  ): HelperContext[Unit] =
    try {
      (for {
          validatedInput <- validate()
          initializedInput <- initialize(validatedInput)
          proceedOrMocked <- mockOrProceed()
          output <- runWork(validatedInput, proceedOrMocked)
          allOutputs = camundaOutputs(
            validatedInput,
            initializedInput,
            output
          )
          filteredOut <- filteredOutput(allOutputs)
        } yield externalTaskService.handleSuccess(filteredOut) //
      ).left.map{
        case ex =>
           externalTaskService.handleError(ex)
      }
    } catch { // safety net
      case ex: Throwable =>
        ex.printStackTrace()
        externalTaskService.handleError(
          UnexpectedError(errorMsg =
            s"We caught an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
          )
        )
    }

  end executeWorker

  protected def defaultMock: Out
  protected def defaultHandledErrorCodes =
    Seq(ErrorCodes.`output-mocked`, ErrorCodes.`validation-failed`)

  override protected def getDefaultMock: MockerOutput = Left(MockedOutput(toCamunda(defaultMock)))

  //TODO always set initialized input - for the default values
  private def camundaOutputs(
      initializedInput: In,
      internalVariables: Map[String, Any],
      output: Option[Out]
  ): Map[String, Any] =
    toCamunda(initializedInput) ++ internalVariables ++ output
      .map(toCamunda)
      .getOrElse(Map.empty)

  extension (externalTaskService: ExternalTaskService)

    private def handleSuccess(
        filteredOutput: Map[String, Any]
    ): HelperContext[Unit] =
      externalTaskService.complete(
        summon[ExternalTask],
        filteredOutput.asJava,
        Map.empty.asJava
      )

    private def handleError(
        error: CamundalaWorkerError
    ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      (for{
        handledErrors <- extractSeqFromArrayOrString(InputParams.handledErrors)
        regexHandledErrors <- extractSeqFromArrayOrString(InputParams.regexHandledErrors)
        errorHandled = error.isMock || handledErrors.contains(error.errorCode.toString)
        errorRegexHandled = errorHandled && regexHandledErrors.forall(regex => error.errorMsg.matches(s".*$regex.*"))
      } yield (errorHandled, errorRegexHandled))
        .flatMap{
          case (true, true) =>
            val mockedOutput = error match
              case error: ErrorWithOutput =>
                error.mockedOutput
              case _ => Map.empty
            filteredOutput(mockedOutput)
              .map(filtered =>
                externalTaskService.handleBpmnError(
                  summon[ExternalTask],
                  s"${error.errorCode}",
                  error.errorMsg,
                  filtered.asJava
                )
              )
          case (true, false) =>
            Left(HandledRegexNotMatchedError(error))
          case _ =>
            Left(error)
        }.left.map{ err =>
          val errMessage = s"${err.errorCode}: ${err.errorMsg}"
          externalTaskService.handleFailure(
            summon[ExternalTask],
            errMessage,
            s" ${errMessage}\nSee the log of the Worker: ${niceClassName(worker.getClass)}",
            0,
            0
          ) //TODO implement retry mechanism
        }
    end handleError

  end extension

  private def filteredOutput(
                              allOutputs: Map[String, Any]
                            ): HelperContext[Either[BadVariableError, Map[String, Any]]] =
    extractSeqFromArrayOrString(InputParams.outputVariables)
      .map {
        case filter if filter.isEmpty => allOutputs
        case filter =>
          allOutputs
            .filter { case k -> _ => filter.contains(k) }
      }

  override protected def initialize(
      inputObject: In
  ): InitializerOutput =
    serviceDefaults()

  // serviceName is not needed anymore
  protected def serviceDefaults(
      initVariables: Map[String, Any] = Map.empty
  ): Right[Nothing, Map[String, Any]] =
    Right(
      initVariables ++ Map(
        "serviceName" -> "NOT-USED"
      )
    )
end CamundalaWorker
