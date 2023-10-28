package camundala.worker

import camundala.domain.*
import camundala.worker.CamundalaWorkerError.{ServiceRequestError, requestMsg, serviceErrorMsg}

trait WorkRunner[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
]:
  type RunnerOutput =
    EngineContext ?=> Either[CamundalaWorkerError, Option[Out]]

  def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput

case class NoWorkRunner[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec
]() extends WorkRunner[In, Out]:
  def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput = Right(None)

end NoWorkRunner

case class ServiceRunner[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn <: Product: CirceCodec,
    ServiceOut: CirceCodec
](
   worker: ServiceWorker[In, Out, ServiceIn, ServiceOut],
   requestHandler: ServiceHandler[In, Out, ServiceIn, ServiceOut],
                         )(using context: EngineContext)
    extends WorkRunner[In, Out]:

  def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput =
    val runnableRequest = RunnableRequest(inputObject, requestHandler)
    for {
      optWithServiceMock <- withServiceMock(optOutMock, runnableRequest)
      output <- handleMocking(optWithServiceMock, runnableRequest).getOrElse(
        requestHandler
          .sendRequest(runnableRequest)
          .flatMap(requestHandler.outputMapper)
      )
    } yield output

  private def withServiceMock(
      optOutMock: Option[Out],
      runnableRequest: RunnableRequest[ServiceIn]
  )(using context: EngineContext): Either[CamundalaWorkerError, Option[Out]] =
    context.generalVariables.outputServiceMockOpt
      .map { json =>
        for {
          mockedResponse <- decodeMock[MockedServiceResponse[ServiceOut]](
            true,
            json
          )
          out <- handleServiceMock(
            mockedResponse,
            runnableRequest
          )
        } yield out

      }
      .getOrElse(Right(None))
  end withServiceMock

  private def handleMocking(
      optOutMock: Option[Out],
      runnableRequest: RunnableRequest[ServiceIn]
  ): Option[Either[CamundalaWorkerError, Option[Out]]] =
    optOutMock
      .map { mock =>
        println(s"""Mocked Service: ${niceClassName(this.getClass)}
                   |${requestMsg(runnableRequest)}
                   | - mockedResponse: ${mock.asJson}
                   |""".stripMargin)
        mock
      }
      .map(m => Right(Some(m)))
  end handleMocking

  private def handleServiceMock(
      mockedResponse: Option[MockedServiceResponse[ServiceOut]],
      runnableRequest: RunnableRequest[ServiceIn]
  ): Either[CamundalaWorkerError, Option[Out]] =
    mockedResponse
      .map {
        case MockedServiceResponse(_, Right(body), headers) =>
          mapBodyOutput(body, headers)
        case MockedServiceResponse(status, Left(body), _) =>
          Left(
            ServiceRequestError(
              status,
              serviceErrorMsg(
                status,
                s"Mocked Error: ${body.asJson}",
                runnableRequest
              )
            )
          )
      }
      .getOrElse(Right(None))

  def mapBodyOutput(
                     serviceOutput: ServiceOut,
                     headers: Seq[Seq[String]]
                   ) =
    requestHandler.outputMapper(
      RequestOutput(
        serviceOutput,
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      )
    )

end ServiceRunner
