package camundala
package examples.invoice

import camundala.bpmn.*
import camundala.bpmn.GenericExternalTask.ProcessStatus
import camundala.domain.*
import io.github.iltotore.iron.constraint.string.Alphanumeric
import sttp.tapir.Schema.annotations.description

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.circe.given
import sttp.tapir.codec.iron.given

object StarWarsRestApi extends BpmnDsl:

  final val topicName = "star-wars-api-people-detail"
  type ServiceOut = People
  lazy val serviceMock: MockedServiceResponse[ServiceOut] = MockedServiceResponse.success200(People(), Map("fromHeader" -> "okidoki"))

  @description("Same Input as _InvoiceReceipt_, only different Mocking")
  case class In(
      id: Int = 1
  )

  object In:
    given Schema[In] = Schema.derived
    given CirceCodec[In] = deriveCodec
  end In

  enum Out derives ConfiguredCodec:
    case Success(
      people: People = People(),
      fromHeader: String = "okidoki",
      val processStatus: ProcessStatus =  ProcessStatus.succeeded
    )
    case Failure(val processStatus: ProcessStatus =  ProcessStatus.`404`)

  object Out:
    given Schema[Out] = Schema.derived
  end Out

  case class People(
                     name: String = "Luke Skywalker",
                     height: String = "172",
                     mass: String = "77",
                     hair_color: String :| Alphanumeric = "blond",
                     skin_color: String = "fair",
                     eye_color: String = "blue"
  )
  object People:
    given Schema[People] = Schema.derived
    given CirceCodec[People] = deriveCodec
  end People

  final lazy val example: ServiceTask[In, Out, ServiceOut] =
    serviceTask(
      topicName,
      descr = "Get People Details from StarWars API",
      in = In(),
      out = Out.Success(),
      defaultServiceOutMock = serviceMock,
    )

end StarWarsRestApi
