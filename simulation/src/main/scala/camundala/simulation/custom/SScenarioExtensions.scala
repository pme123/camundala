package camundala.simulation
package custom

import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*
import io.circe.*
import io.circe.syntax.*
import sttp.client3.*

import scala.collection.immutable.Seq

trait SScenarioExtensions extends SStepExtensions:

  case class ProcessInstanceOrExecution(
      execution: Option[Execution],
      processInstance: Option[ProcessInstance]
  )
  object ProcessInstanceOrExecution:
    given Schema[ProcessInstanceOrExecution] = Schema.derived
    given Encoder[ProcessInstanceOrExecution] = deriveEncoder
    given Decoder[ProcessInstanceOrExecution] = deriveDecoder

  case class Execution(processInstanceId: String)
  object Execution:
    given Schema[Execution] = Schema.derived
    given Encoder[Execution] = deriveEncoder
    given Decoder[Execution] = deriveDecoder

  case class ProcessInstance(id: String)
  object ProcessInstance:
    given Schema[ProcessInstance] = Schema.derived
    given Encoder[ProcessInstance] = deriveEncoder
    given Decoder[ProcessInstance] = deriveDecoder

  extension (scenario: IsProcessScenario)

    def startProcess()(using
        data: ScenarioData
    ): ResultType = {
      val process = scenario.process
      val body = StartProcessIn(
        process.camundaInMap,
        businessKey = Some(scenario.name)
      ).asJson.deepDropNullValues.toString
      val uri =
        uri"${config.endpoint}/process-definition/key/${process.id}${config.tenantPath}/start"

      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)

      runRequest(request, s"Process '${scenario.name}' startProcess")(
        (body, data) =>
          body.hcursor
            .downField("id")
            .as[String]
            .map { processInstanceId =>
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
    }

    def sendMessage()(using
        data: ScenarioData
    ): ResultType = {
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

      runRequest(request, s"Process '${scenario.name}' start with message")(
        (body, data) =>
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
    }

  end extension

  extension (scenario: ProcessScenario)
    def run(): ResultType =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data
        for
          given ScenarioData <- scenario.startType match
            case ProcessStartType.START => scenario.startProcess()
            case ProcessStartType.MESSAGE => scenario.sendMessage()
          given ScenarioData <- scenario.runSteps()
          given ScenarioData <- scenario.check()
        yield summon[ScenarioData]
      }
  end extension

  extension (scenario: IncidentScenario)
    def run(): ResultType =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        for
          given ScenarioData <- scenario.startProcess()
          given ScenarioData <- scenario.runSteps()
          given ScenarioData <- checkIncident()(summon[ScenarioData])
        yield summon[ScenarioData]
      }

    def checkIncident()(data: ScenarioData): ResultType = {
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/incident?processInstanceId=$processInstanceId&deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)
      given ScenarioData = data
      runRequest(request, s"Process '${scenario.name}' checkIncident") {
        (body, data) =>
          body.hcursor.values
            .map {
              case (values: Iterable[Json]) if values.toSeq.nonEmpty =>
                body.hcursor.downArray
                  .downField("incidentMessage")
                  .as[String]
                  .left
                  .map { ex =>
                    data
                      .error(
                        s"Problem extracting incidentMessage from $body\n $ex"
                      )
                  }
                  .flatMap {
                    case incidentMessage
                        if incidentMessage.contains(scenario.incidentMsg) =>
                      Right(
                        data
                          .info(
                            s"Process ${scenario.name} has finished with incident (as expected)."
                          )
                      )
                    case incidentMessage =>
                      Left(
                        data.error(
                          "The Incident contains not the expected message." +
                            s"\nExpected: ${scenario.incidentMsg}\nActual Message: $incidentMessage"
                        )
                      )
                  }
              case _ =>
                given ScenarioData = data
                tryOrFail(checkIncident(), scenario)
            }
            .getOrElse(
              Left(data.error("An Array is expected (should not happen)."))
            )
      }
    }
    end checkIncident

  end extension

  extension (scenario: SScenario)
    def logScenario(body: ScenarioData => ResultType): ResultType =
      val startTime = System.currentTimeMillis()
      val data = ScenarioData(logEntries =
        Seq(info(s"${"#" * 7} Scenario '${scenario.name}' ${"#" * 7}"))
      )
      body(data)
        .map(
          _.info(
            s"${Console.GREEN}${"*" * 4} Scenario '${scenario.name}' SUCCEEDED in ${System.currentTimeMillis() - startTime} ms ${"*" * 4}${Console.RESET}"
          )
        )
        .left
        .map(
          _.error(
            s"${Console.RED}${"*" * 4} Scenario '${scenario.name}' FAILED in ${System.currentTimeMillis() - startTime} ms ${"*" * 6}${Console.RESET}"
          )
        )
    end logScenario
  end extension

end SScenarioExtensions
