package camundala.simulation
package custom

import camundala.bpmn.*
import camundala.domain.*
import io.circe.*
import sttp.client3.*

import scala.collection.immutable.Seq
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait SScenarioExtensions extends SStepExtensions:

  case class ProcessInstanceOrExecution(
      execution: Option[Execution],
      processInstance: Option[ProcessInstance]
  )
  object ProcessInstanceOrExecution:
    given ApiSchema[ProcessInstanceOrExecution] = deriveApiSchema
    given InOutCodec[ProcessInstanceOrExecution] = deriveInOutCodec

  case class Execution(processInstanceId: String)
  object Execution:
    given ApiSchema[Execution] = deriveApiSchema
    given InOutCodec[Execution] = deriveInOutCodec

  case class ProcessInstance(id: String)
  object ProcessInstance:
    given ApiSchema[ProcessInstance] = Schema.derived
    given InOutCodec[ProcessInstance] = deriveInOutCodec

  extension (scenario: IsProcessScenario)

    def startProcess()(using
        data: ScenarioData
    ): ResultType =
      val request = prepareStartProcess()

      runRequest(request, s"Process '${scenario.name}' startProcess")((body, data) =>
        body.hcursor
          .downField("id")
          .as[String]
          .map { processInstanceId =>
            data
              .withProcessInstanceId(processInstanceId)
              .info(
                s"Process '${scenario.process.processName}' started (check $cockpitUrl/#/process-instance/$processInstanceId)"
              )
              .debug(s"- processInstanceId: $processInstanceId")
              .debug(s"- body: $body")
          }
          .left
          .map { ex =>
            data
              .error(s"Problem extracting processInstanceId from $body\n $ex")
          }
      )
    end startProcess

    def sendMessage()(using
        data: ScenarioData
    ): ResultType =
      val process = scenario.process
      val body = CorrelateMessageIn(
        messageName = process.processName,
        tenantId = summon[SimulationConfig[?]].tenantId,
        businessKey = Some(scenario.name),
        processVariables = Some(process.camundaInMap)
      ).asJson.deepDropNullValues.toString
      val uri = uri"${config.endpoint}/message"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      runRequest(request, s"Process '${scenario.name}' start with message")((body, data) =>
        body
          .as[List[ProcessInstanceOrExecution]]
          .map { pioe =>
            val processInstanceId = pioe match
              case ProcessInstanceOrExecution(Some(exec), None) :: _ =>
                exec.processInstanceId
              case ProcessInstanceOrExecution(None, Some(procInst)) :: _ =>
                procInst.id
              case other => s"PROCESS ID not found in $other"
            data
              .withProcessInstanceId(processInstanceId)
              .info(
                s"Process '${process.processName}' started (check $cockpitUrl/#/process-instance/$processInstanceId)"
              )
              .debug(s"- processInstanceId: $processInstanceId")
              .debug(s"- body: $body")
          }
          .left
          .map { ex =>
            data
              .error(s"Problem extracting processInstanceId from $body\n $ex")
          }
      )
    end sendMessage

    private def prepareStartProcess() =
      val process = scenario.process
      val body = StartProcessIn(
        process.camundaInMap,
        businessKey = Some(scenario.name)
      ).asJson.deepDropNullValues.toString
      val uri = config.tenantId match
        case Some(tenantId) =>
          uri"${config.endpoint}/process-definition/key/${process.processName}/tenant-id/$tenantId/start"
        case None =>
          uri"${config.endpoint}/process-definition/key/${process.processName}/start"

      val request: Request[Either[String, String], Any] = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)
      request
    end prepareStartProcess
  end extension

  extension (scenario: ProcessScenario)
    def run(): Future[ResultType] =
      println(s"ProcessScenario started ${scenario.name}")
      scenario.logScenario { (data: ScenarioData) =>
        if scenario.process == null then
          Left(
            data.error(
              "The process is null! Check if your variable is LAZY (`lazy val myProcess = ...`)."
            )
          )
        else
          given ScenarioData = data
          for
            given ScenarioData <- scenario.startType match
              case ProcessStartType.START => scenario.startProcess()
              case ProcessStartType.MESSAGE => scenario.sendMessage()
            given ScenarioData <- scenario.runSteps()
            given ScenarioData <- scenario.check()
          yield summon[ScenarioData]
          end for
      }
  end extension
  extension (scenario: ExternalTaskScenario)
    def run(): Future[ResultType] =
      println(s"ExternalTaskScenario started ${scenario.name}")
      scenario.logScenario { (data: ScenarioData) =>
        if scenario.process == null then
          Left(
            data.error(
              "The process is null! Check if your variable is LAZY (`lazy val myProcess = ...`)."
            )
          )
        else
          given ScenarioData = data
          for
            given ScenarioData <- scenario.startType match
              case ProcessStartType.START => scenario.startProcess()
              case ProcessStartType.MESSAGE => scenario.sendMessage()
            given ScenarioData <- scenario.check()
          yield summon[ScenarioData]
          end for
      }
  end extension

  extension (scenario: IsIncidentScenario)
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        for
          given ScenarioData <- scenario.startProcess()
          given ScenarioData <- scenario.runSteps()
          given ScenarioData <- checkIncident()(
            summon[ScenarioData].withRequestCount(0)
          )
        yield summon[ScenarioData]
        end for
      }

    def checkIncident(
        rootIncidentId: Option[String] = None
    )(data: ScenarioData): ResultType =
      scenario.handleIncident(rootIncidentId)(data) { (body, data) =>
        body.hcursor.values
          .map {
            case (values: Iterable[Json]) if values.toSeq.nonEmpty =>
              scenario.extractIncidentMsg(body)(data)
                .flatMap {
                  case (Some(incidentMessage), _, _)
                      if incidentMessage.contains(scenario.incidentMsg) =>
                    Right(
                      data
                        .info(
                          s"Process ${scenario.name} has finished with incident (as expected)."
                        )
                    )
                  case (Some(incidentMessage), _, _) =>
                    Left(
                      data.error(
                        "The Incident contains not the expected message." +
                          s"\nExpected: ${scenario.incidentMsg}\nActual Message: $incidentMessage"
                      )
                    )
                  case (None, id, rootCauseIncidentId)
                      if id != rootCauseIncidentId =>
                    checkIncident(Some(rootCauseIncidentId))(
                      data.info(
                        s"Incident Message only in Root incident $rootCauseIncidentId"
                      )
                    )
                  case _ =>
                    Left(
                      data
                        .error(
                          "The Incident does not contain any incidentMessage."
                        )
                    )
                }
            case _ =>
              given ScenarioData = data
              scenario.tryOrFail(checkIncident())
          }
          .getOrElse(
            Left(data.error("An Array is expected (should not happen)."))
          )
      }
    end checkIncident

  end extension

  extension (scenario: BadScenario)
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data
        startProcess()
      }

    def startProcess()(using
        data: ScenarioData
    ): ResultType =
      val request = scenario.prepareStartProcess()

      given ScenarioData = data
        .info(s"Process '${scenario.name}' startProcess")
        .debug(s"- URI: ${request.uri}")
      val response = request.send(backend)

      (response.code.code match
        case scenario.status =>
          Right(
            data.info(s"Status matched for BadScenario (${scenario.status})")
          )
        case other =>
          Left(
            data.error(
              s"Status NOT matched for BadScenario (expected: ${scenario.status}, actual: $other)"
            )
          )
      ).flatMap { data =>
        scenario.errorMsg
          .map(errMsg =>
            response.body match
              case Left(body) if body.contains(errMsg) =>
                Right(data.info(s"Body contains correct errorMsg: '$errMsg'"))
              case msg =>
                Left(
                  data
                    .error(s"Error Message not found in Body.")
                    .info(s"- expected msg: $errMsg")
                    .info(s"- body: $msg")
                )
          )
          .getOrElse(
            Right(data) // no message to compare!
          )

      }
    end startProcess
  end extension

  extension (scenario: SScenario)
    def logScenario(body: ScenarioData => ResultType): Future[ResultType] =
      Future {
        val startTime = System.currentTimeMillis()
        if scenario.isIgnored then
          Right(
            ScenarioData(scenario.name)
              .warn(
                s"${Console.MAGENTA}${"#" * 7} Scenario '${scenario.name}'  IGNORED ${"#" * 7}${Console.RESET}"
              )
          )
        else {
          val data = ScenarioData(scenario.name)
            .info(s"${"#" * 7} Scenario '${scenario.name}' ${"#" * 7}")
          body(data)
            .map(
              _.info(
                s"${Console.GREEN}${"*" * 4} Scenario '${scenario.name}' SUCCEEDED in ${System
                    .currentTimeMillis() - startTime} ms ${"*" * 4}${Console.RESET}"
              )
            )
            .left
            .map(
              _.error(
                s"${Console.RED}${"*" * 4} Scenario '${scenario.name}' FAILED in ${System
                    .currentTimeMillis() - startTime} ms ${"*" * 6}${Console.RESET}"
              )
            )
        }
        end if
      }
    end logScenario
  end extension

end SScenarioExtensions
