package camundala.simulation.custom

import camundala.api.StartProcessIn
import camundala.bpmn.*
import camundala.simulation.*
import io.circe.*
import io.circe.parser.*
import io.circe.syntax.*
import io.gatling.core.structure.ScenarioContext
import sttp.client3.*

trait CustomSimulation extends App, SimulationHelper, SUserTaskExtensions {

  def run(sim: SSimulation): Unit =
    sim.scenarios
      .map {
        case scen: ProcessScenario => scen -> scen.run()
        case other => other -> Right(Seq(ScenarioData().warn(s"UNSUPPORTED: ${other.name}"))).asInstanceOf[ResultType]
      }
      .map {(scen: SScenario , resultData: ResultType) =>
          val data: ScenarioData = resultData.fold(
           d => d,
           d => d
          )
          data.logEntries.filter(config.logLevel).foreach(println)
          scen.name -> data.logEntries.maxLevel
      }
      .groupBy(_._2)
      .toSeq
      .sortBy(_._1)
      .reverse
      .foreach {
        case LogLevel.ERROR -> scenarios =>
          println("*" * 20)
          println("Simulation FAILED! The following Scenarios failed:")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
          println("Check the logs above.")
        case LogLevel.WARN -> scenarios =>
          println("-" * 20)
          println("Simulation has WARNINGS! Check the following Scenarios:")
          scenarios.foreach { case scen -> _ => println(s"- $scen") }
        case _ => // nothing to do
      }

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

    /*
      info(s"******** Scenario ${scen.name} *************") ++
        startProcess().getOrElse(ScenarioData()).logEntries ++
        scen.steps.flatMap
     */

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
        .flatMap(parse)
        .left.map(body => handleNon2xxResponse(response.code, body, request.toCurl))
         .flatMap { body =>
           body.hcursor.downField("id").as[String]
           .left.map { ex =>
             summon[ScenarioData]
               .error(s"Problem extracting processInstanceId from $body\n $ex")
           }.map{ processInstanceId =>
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

  private def checkLogLevel(entry: LogEntry) =
    entry.logLevel.compare(config.logLevel) < 1

  extension (step: SStep) {
    def run()(using
        data: ScenarioData
    ): ResultType =
      step match {
        case ut: SUserTask =>
          ut.getAndComplete()
        case e: SReceiveMessageEvent =>
          Right(data.info(s"e ${e.name}"))
        // e.correlate(config.tenantId)
        case e: SReceiveSignalEvent =>
          Right(data.info(s"e ${e.name}"))
        // e.sendSignal()
        case sp: SSubProcess =>
          Right(data.error(s"sp ${sp.name}"))
        /*  sp.switchToSubProcess() ++
             sp.steps.flatMap(toGatling) ++
             sp.check() :+
             sp.switchToMainProcess() */
        case SWaitTime(seconds) =>
          Right(data.info(s"wait time $seconds"))
        //  Seq(exec().pause(seconds))

      }
  }
}
