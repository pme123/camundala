package camundala
package camunda7.worker

import camundala.bpmn.*
import camundala.worker.CamundalaWorkerError.*
import camundala.domain.*
import camundala.worker.*
import sttp.client3.*
import sttp.model.Uri

trait ServiceWorker[
    In <: Product: CirceCodec,
    Out <: Product: CirceCodec,
    ServiceIn: Encoder, // body of service
    ServiceOut: Decoder // output of service
] extends CamundalaWorker[
      In,
      Out
    ]: //,      RestApiClient[ServiceIn, ServiceOut]:

  type BodyType = HelperContext[Either[CamundalaWorkerError, Option[ServiceIn]]]
  type OutputType = HelperContext[Either[CamundalaWorkerError, Option[Out]]]

  protected def httpMethod: Method

  protected def serviceBasePath: String

  protected def servicePath(inputObject: In): os.RelPath = os.rel


  // default is no params
  // a list with keys or a list with keys and its defaults
  protected def queryParamKeys: Seq[String | (String, String)] = Seq.empty

  private lazy val defaultsMap = queryParamKeys.map {
    case k -> v => k -> Some(v)
    case k => k -> None
  }.toMap

  private def queryParams(inputObject: In): Seq[(String, Seq[String])] =
    inputObject.productElementNames.toSeq.zip(inputObject.productIterator.toSeq)
      .collect{
        case k -> Some(value) if defaultsMap.contains(k) =>
          k -> Seq(s"$value")

        case k -> None if defaultsMap.get(k).flatten.isDefined =>
          k -> Seq(defaultsMap.get(k).flatten.get)

      }
  // default is no body
  protected def mapBodyInput(inputObject: In): BodyType = Right(None)

  // default is no output
  protected def mapBodyOutput(
      outputBody: ServiceOut,
      headers: Map[String, String]
  ): OutputType = Right(None)
  
  // default is no output
  protected def mapBodyOutput(
      requestOutput: RequestOutput[ServiceOut]
  ): OutputType = mapBodyOutput(requestOutput.outputBodyOpt.get, requestOutput.headers)

  protected def mapBodyOutput(
      serviceOutput: ServiceOut,
      headers: Seq[Seq[String]]
  ): OutputType =
    mapBodyOutput(
      RequestOutput(
        Some(serviceOutput),
        // take correct ones and make a map of it
        headers
          .map(_.toList)
          .collect { case key :: value :: _ => key -> value }
          .toMap
      )
    )

  protected def defaultServiceMock: ServiceOut
  protected def defaultHeaders: Map[String, String] = Map.empty

  override protected def defaultMock: Out =
    ??? // not needed - by default test mapping with service output
  override protected def defaultHandledErrorCodes = Seq(
    ErrorCodes.`output-mocked`
  ) // validation is not handled for services

  override protected def getDefaultMock: MockerOutput =
    mapBodyOutput(RequestOutput(Some(defaultServiceMock), defaultHeaders)).left.map(
      err => MockerError(errorMsg = err.errorMsg)
    )

  protected val isService: Boolean = true

  protected def sendRequest(
      request: Request[Either[String, String], Any],
      optReqBody: Option[ServiceIn]
  ): Either[ServiceError, RequestOutput[ServiceOut]]

  override protected def runWork(
      inputObject: In,
      optOutMock: Option[Out]
  ): RunnerOutput =
    for {
      body <- mapBodyInput(inputObject)
      path = servicePath(inputObject)
      uri = uri"$serviceBasePath/$path"
      qParams = queryParams(inputObject)
      optWithServiceMock <- withServiceMock(optOutMock, uri, qParams, body)
      output <- handleMocking(optWithServiceMock, uri, qParams, body).getOrElse(
        sendRequest(requestMethod(uri, qParams), body)
          .flatMap(mapBodyOutput)
      )
    } yield output

  end runWork

  private def withServiceMock(
      optOutMock: Option[Out],
      apiUri: Uri,
      queryParams: Seq[(String, Seq[String])],
      requestBody: Option[ServiceIn]
  ): HelperContext[Either[CamundalaWorkerError, Option[Out]]] =
    val outputServiceMock =
      jsonVariableOpt(InputParams.outputServiceMock)
    outputServiceMock
      .flatMap {
        case Some(json) =>
          for {
            mockedResponse <- decodeMock[MockedServiceResponse[ServiceOut]](
              json
            )
            out <- handleServiceMock(
              mockedResponse,
              apiUri,
              queryParams,
              requestBody
            )
          } yield out
        case None => Right(optOutMock)
      }
  end withServiceMock

  private def handleMocking(
      optOutMock: Option[Out],
      apiUri: Uri,
      queryParams: Seq[(String, Seq[String])],
      requestBody: Option[ServiceIn]
  ): Option[Either[CamundalaWorkerError, Option[Out]]] =
    optOutMock
      .map { mock =>
        println(s"""Mocked Service: ${niceClassName(this.getClass)}
                   |${requestMsg(apiUri, queryParams, requestBody)}
                   | - mockedResponse: ${mock.asJson}
                   |""".stripMargin)
        mock
      }
      .map(m => Right(Some(m)))
  end handleMocking

  private def handleServiceMock(
      mockedResponse: Option[MockedServiceResponse[ServiceOut]],
      apiUri: Uri,
      queryParams: Seq[(String, Seq[String])],
      requestBody: Option[ServiceIn]
  ): HelperContext[Either[CamundalaWorkerError, Option[Out]]] =
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
                apiUri,
                queryParams,
                requestBody
              )
            )
          )
      }
      .getOrElse(Right(None))

  private def requestMethod(
      apiUri: Uri,
      qParams: Seq[(String, Seq[String])]
  ): Request[Either[String, String], Any] =
    basicRequest
      .copy(uri = apiUri.params(QueryParams(qParams)), method = httpMethod)
  end requestMethod
end ServiceWorker


