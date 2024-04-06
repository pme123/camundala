package camundala.examples.invoice.worker

import camundala.examples.invoice.bpmn.ComposedWorkerExample.*
import camundala.examples.invoice.bpmn.{StarWarsPeople, StarWarsPeopleDetail}
import camundala.worker.CamundalaWorkerError.CustomError
import camundala.worker.CustomWorkerDsl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration

@Configuration
class ComposedWorker
    extends CompanyWorkerHandler, // environment specific
    CustomWorkerDsl[In, Out]: // DSL for this type

  @Autowired
  var peopleWorker: StarWarsPeopleWorker = _
  @Autowired
  var peopleDetailWorker: StarWarsPeopleDetailWorker = _

  lazy val customTask = example

  def runWork(
      inputObject: In
  ): Either[CustomError, Out] =
    logger.info("Do some crazy things running work...")
    val peopleWorkerIn = StarWarsPeople.In()
    val out = peopleWorker.runWork(peopleWorkerIn)
    out
      .flatMap:
        case StarWarsPeople.Out.Success(people, _) =>
          import cats.implicits.*
          people.zipWithIndex
            .map:
              case (_, index) =>
                val detail = StarWarsPeopleDetail.In(index)
                peopleDetailWorker.runWork(detail)
            .sequence
        case _ =>
          Right(Seq.empty)
      .map: r =>
        Out(
          r.collect:
            case StarWarsPeopleDetail.Out.Success(detail, _, _) =>
              detail
        )

  end runWork

end ComposedWorker
