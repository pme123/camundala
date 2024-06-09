package camundala.examples.invoice.bpmn

import camundala.bpmn.*
import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.domain.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import sttp.tapir.Schema.annotations.description

trait StarWarsApi extends BpmnServiceTaskDsl:
  val serviceLabel: String = "Star Wars API"
  val serviceVersion: String = "1.0"
  val companyDescr = ""
end StarWarsApi

object StarWarsPeople extends StarWarsApi:

  final val topicName = "star-wars-api-people"
  val descr = "Get People from StarWars API"
  val path: String = "GET /people"

  type ServiceIn = NoInput
  type ServiceOut = PeopleResults
  lazy val serviceInExample = NoInput()
  lazy val serviceMock: MockedServiceResponse[ServiceOut] =
    MockedServiceResponse.success200(PeopleResults())

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      heightMoreThanInCm: Option[Int] = Some(183)
  )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  enum Out:
    case Success(
        people: Seq[People] = Seq(People()),
        val processStatus: ProcessStatus = ProcessStatus.succeeded
    )
    case Failure(val processStatus: ProcessStatus = ProcessStatus.`404`)
  end Out

  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  end Out

  case class PeopleResults(
      results: Seq[People] = Seq(People())
  )

  object PeopleResults:
    given ApiSchema[PeopleResults] = deriveApiSchema
    given InOutCodec[PeopleResults] = deriveCodec
  end PeopleResults

  final lazy val example: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    serviceTask(
      in = In(),
      out = Out.Success(),
      defaultServiceOutMock = serviceMock,
      serviceInExample = serviceInExample
    )

end StarWarsPeople

object StarWarsPeopleDetail extends StarWarsApi:

  final val topicName = "star-wars-api-people-detail"
  val descr = "Get People Details from StarWars API"
  val path: String = "GET /people/${in.id}"

  type ServiceIn = NoInput
  type ServiceOut = People
  lazy val serviceInExample = NoInput()

  lazy val serviceMock: MockedServiceResponse[ServiceOut] =
    MockedServiceResponse.success200(People(), Map("fromHeader" -> "okidoki"))

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1,
      @description("Optional value to test querySegments")
      optName: Option[String] = None
  )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveCodec
  end In

  enum Out:
    case Success(
        people: People = People(),
        fromHeader: String = "okidoki",
        processStatus: ProcessStatus = ProcessStatus.succeeded
    )
    case Failure(val processStatus: ProcessStatus = ProcessStatus.`404`)
  end Out

  object Out:
    given ApiSchema[Out] = deriveApiSchema
    given InOutCodec[Out] = deriveInOutCodec
  end Out

  final lazy val example: ServiceTask[In, Out, ServiceIn, ServiceOut] =
    serviceTask(
      in = In(),
      out = Out.Success(),
      defaultServiceOutMock = serviceMock,
      serviceInExample = serviceInExample
    )

end StarWarsPeopleDetail

case class People(
    name: String = "Luke Skywalker",
    height: String = "172",
    mass: String = "77",
    hair_color: String = "blond",
    skin_color: String = "fair",
    eye_color: String = "blue"
)

object People:
  given ApiSchema[People] = deriveApiSchema
  given InOutCodec[People] = deriveCodec
end People
