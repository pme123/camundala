package camundala.examples.invoice.worker

import camundala.domain.GeneralVariables
import camundala.examples.invoice.bpmn.ComposedWorkerExample.*
import camundala.examples.invoice.bpmn.{StarWarsPeople, StarWarsPeopleDetail}
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.{CustomWorkerDsl, EngineRunContext}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import zio.IO

@Configuration
class ComposedWorker
    extends CompanyCustomWorkerDsl[In, Out]: // DSL for this type

  @Autowired
  var peopleWorker: StarWarsPeopleWorker = scala.compiletime.uninitialized
  @Autowired
  var peopleDetailWorker: StarWarsPeopleDetailWorker = scala.compiletime.uninitialized

  lazy val customTask = example

  def runWork(
      inputObject: In
  ): Either[CustomError, Out] =
    logger.info("Do some crazy things running work...")
    given EngineRunContext =
      EngineRunContext(engineContext, GeneralVariables())

    val peopleWorkerIn = StarWarsPeople.In()
    val out = peopleWorker.runWorkFromWorkerUnsafe(peopleWorkerIn)
      .left.map: error =>
        CustomError(
          s"Error while fetching Starwars People:\n- ${error.errorMsg}."
        )
    out.map:
      case StarWarsPeople.Out.Success(people, _) =>
        logger.info(s"- Got People: $people")
        Out()
      case StarWarsPeople.Out.Failure(processStatus) =>
        logger.info(s"- Got People failed with: $processStatus")
        Out()

  end runWork

end ComposedWorker
