package camundala
package examples.invoice

import camundala.bpmn.*
import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.domain.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.constraint.string.Alphanumeric
import sttp.tapir.Schema.annotations.description
import sttp.tapir.codec.iron.given

object StarWarsPeople extends BpmnDsl:

  final val topicName = "star-wars-api-people"
  type ServiceOut = PeopleResults
  lazy val serviceMock: MockedServiceResponse[ServiceOut] =
    MockedServiceResponse.success200(PeopleResults())

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      heightMoreThanInCm: Option[Int] = Some(183)
  )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  end In

  enum Out derives ConfiguredCodec:
    case Success(
        people: Seq[People] = Seq(People()),
        val processStatus: ProcessStatus = ProcessStatus.succeeded
    )
    case Failure(val processStatus: ProcessStatus = ProcessStatus.`404`)
  end Out

  object Out:
    given ApiSchema[Out] = deriveApiSchema
  end Out

  case class PeopleResults(
      results: Seq[People] = Seq(People())
  )

  object PeopleResults:
    given ApiSchema[PeopleResults] = deriveApiSchema
    given InOutCodec[PeopleResults] = deriveInOutCodec
  end PeopleResults

  final lazy val example: ServiceTask[In, Out, ServiceOut] =
    serviceTask(
      topicName,
      descr = "Get People from StarWars API",
      in = In(),
      out = Out.Success(),
      defaultServiceOutMock = serviceMock
    )

end StarWarsPeople

object StarWarsPeopleDetail extends BpmnDsl:

  final val topicName = "star-wars-api-people-detail"
  type ServiceOut = People
  lazy val serviceMock: MockedServiceResponse[ServiceOut] =
    MockedServiceResponse.success200(People(), Map("fromHeader" -> "okidoki"))

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1,
  )

  object In:
    given ApiSchema[In] = deriveApiSchema
    given InOutCodec[In] = deriveInOutCodec
  end In

  enum Out derives ConfiguredCodec:
    case Success(
        people: People = People(),
        fromHeader: String = "okidoki",
        val processStatus: ProcessStatus = ProcessStatus.succeeded
    )
    case Failure(val processStatus: ProcessStatus = ProcessStatus.`404`)
  end Out

  object Out:
    given ApiSchema[Out] = deriveApiSchema
  end Out
  final lazy val example: ServiceTask[In, Out, ServiceOut] =
    serviceTask(
      topicName,
      descr = "Get People Details from StarWars API",
      in = In(),
      out = Out.Success(),
      defaultServiceOutMock = serviceMock
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
  given InOutCodec[People] = deriveInOutCodec
end People
