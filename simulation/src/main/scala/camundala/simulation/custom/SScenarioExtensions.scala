package camundala.simulation
package custom

import camundala.api.*
import camundala.bpmn.*
import camundala.domain.*
import io.circe.*
import io.circe.syntax.*
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
      val request = prepareStartProcess()

      runRequest(request, s"Process '${scenario.name}' startProcess")(
        (body, data) =>
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

    def prepareStartProcess() =
      val process = scenario.process
      if(process == null)
        Left("The process is null! Check if your variable is LAZY (`lazy val myProcess = ...`).")
      else
        val body = StartProcessIn(
          process.camundaInMap,
          businessKey = Some(scenario.name)
        ).asJson.deepDropNullValues.toString
        val uri = config.tenantId match
          case Some(tenantId) =>
            uri"${config.endpoint}/process-definition/key/${process.id}/tenant-id/$tenantId/start"
          case None =>
            uri"${config.endpoint}/process-definition/key/${process.id}/start"

        val request = basicRequest
          .auth()
          .contentType("application/json")
          .body(body)
          .post(uri)
        request
    end prepareStartProcess
  end extension

  extension (scenario: ProcessScenario)
    def run(): Future[ResultType] =
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
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data

        for
          given ScenarioData <- scenario.startProcess()
          given ScenarioData <- scenario.runSteps()
          given ScenarioData <- checkIncident()(summon[ScenarioData])
        yield summon[ScenarioData]
      }

    def checkIncident(
        rootIncidentId: Option[String] = None
    )(data: ScenarioData): ResultType = {
      val processInstanceId = data.context.processInstanceId
      val uri = rootIncidentId match
        case Some(incId) =>
          uri"${config.endpoint}/incident?incidentId=$incId&deserializeValues=false"
        case None =>
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
                val arr = body.hcursor.downArray
                (for
                  maybeIncMessage <- arr
                    .downField("incidentMessage")
                    .as[Option[String]]
                  id <- arr.downField("id").as[String]
                  rootCauseIncidentId <- arr
                    .downField("rootCauseIncidentId")
                    .as[String]
                yield (maybeIncMessage, id, rootCauseIncidentId)).left
                  .map { ex =>
                    data
                      .error(
                        s"Problem extracting incidentMessage from $body\n $ex"
                      )
                      .info(request.toCurl)
                  }
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
                          .info(request.toCurl)
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

  extension (scenario: BadScenario)
    def run(): Future[ResultType] =
      scenario.logScenario { (data: ScenarioData) =>
        given ScenarioData = data
        startProcess()
      }

    def startProcess()(using
        data: ScenarioData
    ): ResultType = {
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
    }
  end extension

  extension (scenario: SScenario)
    def logScenario(body: ScenarioData => ResultType): Future[ResultType] =
      Future {
        val startTime = System.currentTimeMillis()
        if (scenario.isIgnored)
          Right(ScenarioData(scenario.name)
            .warn(s"${Console.MAGENTA}${"#" * 7} Scenario '${scenario.name}'  IGNORED ${"#" * 7}${Console.RESET}"))
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
      }
    end logScenario
  end extension

end SScenarioExtensions
