package camundala.camunda7.worker

import camundala.camunda7.worker.CamundalaWorkerError.*
import camundala.domain.*
import io.circe.*
import io.circe.syntax.*

/** Mocks this - with the following logic:
  *   - if process variable `outputMock` is set - sets its values to the process
  *   - if process variable `serviceMocked` AND the flag `isService` is set -
  *     sets the values of the `defaultMock` to the process
  *   - else nothing is done and the flag doProceed is `true`
  */
trait Mocker[Out <: Product: CirceCodec] extends CamundaHelper:
  protected def isService: Boolean
  protected type MockerOutput = HelperContext[Either[MockerError, Option[Out]]]
  protected def getDefaultMock: MockerOutput

  def mockOrProceed(
  ): HelperContext[Either[MockerError | MockedOutput, Option[Out]]] =
    val servicesMocked = variable(InputParams.servicesMocked, false)
    val outputMockOpt = variableOpt[String](InputParams.outputMock)

    (servicesMocked, outputMockOpt) match {
      case (_, Some(outputMock)) => // if the outputMock is set than we mock
        handleMock(outputMock)
      case (true, _)
          if isService => // if your process is a Service check if it is mocked
        println(s"Mocked - isService is set")
        getDefaultMock
      case (_, None) =>
        Right(None)
    }
  end mockOrProceed

  private def handleMock(
      outputMock: Any
  ): Either[MockerError | MockedOutput, Option[Out]] =
    println("Mocked - outputMock is set")
    parsedMock(outputMock)
      .flatMap(decodeMock)

  end handleMock

  protected def parsedMock(outputMock: Any): Either[MockerError, Json] =
    parser
      .parse(outputMock.toString)
      .left
      .map(exception =>
        MockerError(errorMsg =
          s"The mock could not be parsed to Json Object:\n- $outputMock\n- $exception"
        )
      )
  end parsedMock

  protected def decodeMock[A <: Product: Decoder](
      json: Json
  ): Either[MockerError | MockedOutput, Option[A]] =
    (json.isObject, isService) match
      case (true, true) =>
        decodeTo(json.asJson.toString)
          .map(Some(_))
          .left
          .map(ex => MockerError(errorMsg = ex.errorMsg))
      case (true, _) =>
        Left(
          MockedOutput(mockedOutput =
            json.asObject.get.toMap
              .map { case k -> json =>
                k -> camundaVariable(json)
              }
          )
        )
      case _ =>
        Left(
          MockerError(errorMsg =
            s"The mock must be a Json Object:\n- $json\n- ${json.getClass}"
          )
        )
  end decodeMock
end Mocker
