package camundala.camunda7.worker

import camundala.bpmn.*
import camundala.worker.*
import camundala.worker.CamundalaWorkerError.*
import camundala.domain.*
import io.circe.*
import org.camunda.bpm.client.task.ExternalTask

/** Mocks this - with the following logic:
  *   - if process variable `outputMock` is set - sets its values to the process
  *   - if process variable `serviceMocked` AND the flag `isService` is set -
  *     sets the values of the `defaultMock` to the process
  *   - else nothing is done and the flag doProceed is `true`
  */
trait Mocker[Out <: Product: CirceCodec] extends CamundaHelper:
  protected def isService: Boolean
  protected type MockerOutput = HelperContext[Either[MockerError | MockedOutput, Option[Out]]]
  protected def getDefaultMock: MockerOutput

  def mockOrProceed(
  ): HelperContext[Either[MockerError | MockedOutput, Option[Out]]] =
    (for {
      servicesMocked <- variable(InputParams.servicesMocked, false)
      mockedSubprocesses <- variable(
        InputParams.mockedSubprocesses,
        Seq.empty[String]
      )
      outputMockOpt <- jsonVariableOpt(InputParams.outputMock)
    } yield (servicesMocked, mockedSubprocesses.contains(topicName), outputMockOpt) match
      case (_, _, Some(outputMock)) => // if the outputMock is set than we mock
        decodeMock(outputMock)
      case (_, true, _)
          if !isService => // if your process is NOT a Service check if it is mocked
        getDefaultMock
      case (true, _, _)
          if isService => // if your process is a Service check if it is mocked
        getDefaultMock
      case (_, _, None) =>
        Right(None)
    ).left.map(err => MockerError(err.errorMsg))
      .flatten

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
          s"The mock could not be parsed to Json Object:\n- $exception\n- $outputMock"
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
