package camundala.simulation.custom

import camundala.api.*
import camundala.simulation.*
import io.circe.syntax.*
import sttp.client3.*
import io.circe.*

trait ProcessScenarioExtensions extends SStepExtensions :

  extension (scen: ProcessScenario)
  
    def run(): ResultType =
      given ScenarioData = ScenarioData(logEntries =
        Seq(info(s"******** Scenario ${scen.name} *************"))
      )
  
      for {
        given ScenarioData <- startProcess()
        given ScenarioData <- runSteps()
      } yield (summon[ScenarioData])
  
    def runSteps()(using
                   data: ScenarioData
    ): ResultType =
      scen.steps.foldLeft[ResultType](Right(data)) {
        case (Right(data), step) =>
          given ScenarioData = data
  
          step.run()
        case (leftData, _) => leftData
  
      }
  
    def startProcess()(using
                       data: ScenarioData
    ): ResultType = {
      val process = scen.process
      val backend = HttpClientSyncBackend()
      val body = StartProcessIn(
        process.camundaInMap,
        businessKey = Some(scen.name)
      ).asJson.deepDropNullValues.toString
      val uri =
        uri"${config.endpoint}/process-definition/key/${process.id}${config.tenantPath}/start"
  
      given ScenarioData = data
        .info(s"URI: $uri")
        .debug(s"Body: $body")
  
      val request = basicRequest
        .auth()
        .contentType("application/json")
        .body(body)
        .post(uri)
  
      val response = request.send(backend)
      response.body
        .flatMap(parser.parse)
        .left.map(body => handleNon2xxResponse(response.code, body, request.toCurl))
        .flatMap { body =>
          body.hcursor.downField("id").as[String]
            .left.map { ex =>
            summon[ScenarioData]
              .error(s"Problem extracting processInstanceId from $body\n $ex")
          }.map { processInstanceId =>
            summon[ScenarioData]
              .withProcessInstanceId(processInstanceId)
              .info(
                s"Process '${process.processName}' started"
              )
              .debug(s"- processInstanceId: $processInstanceId")
              .debug(s"- body: $body")
          }
        }
    }
  end extension
    
end ProcessScenarioExtensions
  
