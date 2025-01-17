package camundala.worker.c8zio

import camundala.domain.*
import camundala.worker.{CamundalaWorkerError, JobWorker, Worker, printTimeOnConsole}
import io.camunda.zeebe.client.api.response.ActivatedJob
import io.camunda.zeebe.client.api.worker.{JobClient, JobHandler}
import zio.*

import java.util.Date

trait C8Worker[In: InOutDecoder, Out: InOutEncoder] extends JobWorker, JobHandler:
  def topic: String

  lazy val runtime = Runtime.default

  import cats.data.ValidatedNel
  import cats.implicits.*
  def handle(client: JobClient, job: ActivatedJob): Unit =
    Unsafe.unsafe:
      implicit unsafe =>
        runtime.unsafe.run(
          (for
            startDate   <- ZIO.succeed(new Date())
            json        <- ZIO.fromEither(parser.parse(job.getVariables))
            in          <- ZIO.fromEither(
                             customDecodeAccumulating[In](json.hcursor)
                           )
            businessKey <-
              ZIO.fromEither(json.as[BusinessKey].map(_.businessKey.getOrElse("no businessKey")))
            _           <- Console.printLine(
                             s"Worker: ${job.getType} (${job.getWorker}) started > $businessKey"
                           )
            _           <- Console.printLine(s"IN: $in")
            _           <- ZIO.attempt(client.newCompleteCommand(job.getKey).send().join())
            _           <-
              Console.printLine(
                s"Worker: ${job.getType} (${job.getWorker}) ended ${printTimeOnConsole(startDate)} > $businessKey"
              )
          yield ())
        ).getOrThrow()

  def handle2(client: JobClient, job: ActivatedJob): Unit =
    Unsafe.unsafe { implicit unsafe =>
      runtime.unsafe.run(
        for
          businessKey <-
            ZIO.succeed(Option(job.getVariable("businessKey")).getOrElse("no businessKey"))
          _           <- Console.printLine(
                           s"Worker: ${job.getType} (${job.getWorker}) started > $businessKey"
                         )
          json        <- ZIO.fromEither(parser.parse(job.getVariables))

          _         <- Console.printLine(
                         s"Worker: ${job.getType} (${job.getWorker}) started > $businessKey"
                       )
          startDate <- ZIO.succeed(new Date())
          json      <- ZIO.fromEither(parser.parse(job.getVariables))
          _         <- Console.printLine(
                         s"Worker: ${job.getType} (${job.getWorker}) started > $businessKey"
                       )
          _         <- handleJob(client, job)
          _         <-
            Console.printLine(
              s"Worker: ${job.getType} (${job.getWorker}) ended ${printTimeOnConsole(startDate)} > $businessKey"
            )
          _         <- ZIO.attempt(client.newCompleteCommand(job.getKey).send().join())
        yield ()
      ).getOrThrowFiberFailure()
    }

  private def handleJob(
      client: JobClient,
      job: ActivatedJob
  ): ZIO[Any, Any, Any] = Console.printLine("handleJob")
  /*   val variablesExtractor = ProcessVariablesExtractor(job)
    for
        tryProcessVariables <- variablesExtractor.extract(worker.variableNames ++ worker.inConfigVariableNames)
        tryGeneralVariables <- processExtractor.extractGeneral())
        filteredOut         <- worker.executor(using EngineRunContext(engineContext, tryGeneralVariables)).execute(tryProcessVariables)
  val tryProcessVariables                                =
    ProcessVariablesExtractor(job).extract(worker.variableNames ++ worker.inConfigVariableNames)
  val tryGeneralVariables                                = ProcessVariablesExtractor.extractGeneral()
  try
    (for
        generalVariables <- tryGeneralVariables
        context           = EngineRunContext(engineContext, generalVariables)
        filteredOut      <-
          worker.executor(using context).execute(tryProcessVariables)
      yield externalTaskService.handleSuccess(filteredOut, generalVariables.manualOutMapping) //
    ).left.map { ex =>
      externalTaskService.handleError(ex, tryGeneralVariables)
    }
  catch // safety net
    case ex: Throwable =>
      ex.printStackTrace()
      externalTaskService.handleError(
        UnexpectedError(errorMsg =
          s"We caught an UnhandledException: ${ex.getMessage}\n - check the Workers Log."
        ),
        tryGeneralVariables
      )
  end try
  end handleJob*/
  case class BusinessKey(businessKey: Option[String])
  object BusinessKey:
    given InOutCodec[BusinessKey] = deriveInOutCodec

end C8Worker
