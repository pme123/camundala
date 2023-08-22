package camundala.camunda7.worker

import camundala.camunda7.worker.CamundalaWorkerError.*
import camundala.domain.*
import camundala.bpmn.*
import org.camunda.bpm.client.task.{ExternalTask, ExternalTaskHandler, ExternalTaskService}

import java.time.LocalDateTime
import scala.jdk.CollectionConverters.*
import scala.util.Try

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
      } yield externalTaskService.handleSuccess(
        validatedInput,
        initializedInput,
        output
      )).left.map(ex => 
        externalTaskService.handleError(ex))
    } catch { // safety net
      case ex: Throwable =>
        ex.printStackTrace()
        externalTaskService.handleError(
          UnexpectedError(errorMsg =
            s"We catched an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
          )
        )
    }

  end executeWorker

  protected def defaultMock: Out
  protected def defaultHandledErrorCodes =
    Seq(ErrorCodes.`output-mocked`, ErrorCodes.`validation-failed`)

  override protected def getDefaultMock: MockerOutput = Right(Some(defaultMock))
  extension (externalTaskService: ExternalTaskService)

    /*
  //TODO alway set initialized input - for the default values
     */
    private def handleSuccess(
        initializedInput: In,
        internalVariables: Map[String, Any],
        output: Option[Out]
    ): HelperContext[Unit] =
      val allOutputs: Map[String, Any] =
        (toCamunda(initializedInput) ++ internalVariables ++ output
          .map(toCamunda)
          .getOrElse(Map.empty))
      externalTaskService.complete(
        summon[ExternalTask],
        filteredOutput(allOutputs).asJava,
        Map.empty.asJava
      )

    private def handleError(
        error: CamundalaWorkerError
    ): HelperContext[Unit] =
      import CamundalaWorkerError.*
      val handledErrors = handledErrorCodes(
        variableOpt(InputParams.handledErrors)
      )
      println(s"handledErrors: $handledErrors")
      if (handledErrors.contains(error.errorCode))
        val mockedOutput = error match
          case error: ErrorWithOutput =>
            error.mockedOutput
          case _ => Map.empty
        externalTaskService.handleBpmnError(
          summon[ExternalTask],
          s"${error.errorCode}",
          error.errorMsg,
          filteredOutput(mockedOutput).asJava
        )
      else
        externalTaskService.handleFailure(
          summon[ExternalTask],
          s"${error.errorCode}: ${error.errorMsg}",
          s"See the log of the Worker: ${niceClassName(worker.getClass)}",
          0,
          0
        )
    end handleError

    private def filteredOutput(
        allOutputs: Map[String, Any]
    ): HelperContext[Map[String, Any]] =
      val outputVariables: Option[Seq[String]] = 
        variableOpt[String](InputParams.outputVariables)
          .map(_.split(",").map(_.trim))
      outputVariables match
        case None => allOutputs
        case Some(filter) =>
          allOutputs
            .filter { case k -> _ => filter.contains(k) }
  end extension
  
  private def handledErrorCodes(handledCodeStr: Option[String]) =
    def toErrorCode(codeStr: String) =
      Try(ErrorCodes.valueOf(codeStr)).toOption
        .orElse(codeStr.toIntOption)
        .getOrElse(codeStr)
    val customs = handledCodeStr.toSeq
      .flatMap(_.split(",").map(ec => toErrorCode(ec.trim)))
    defaultHandledErrorCodes ++ customs

  override protected def initialize(
                                     inputObject: In
                                   ): InitializerOutput =
    serviceDefaults()

  // serviceName is not needed anymore
  protected def serviceDefaults(initVariables: Map[String, Any] = Map.empty): Right[Nothing, Map[String, Any]] =
    Right(
      initVariables ++ Map(
        "serviceName" -> "NOT-USED"
      )
    )
end CamundalaWorker
