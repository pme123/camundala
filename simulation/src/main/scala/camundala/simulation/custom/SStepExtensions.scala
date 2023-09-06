package camundala.simulation
package custom

import camundala.bpmn.*
import sttp.client3.*

trait SStepExtensions
    extends SUserTaskExtensions,
      SEventExtensions,
      SSubProcessExtensions:

  extension (step: SStep)
    def run()(using
        data: ScenarioData
    ): ResultType =
      step match
        case ut: SUserTask =>
          ut.getAndComplete()
        case e: SMessageEvent =>
          e.sendMessage()
        case e: SSignalEvent =>
          e.sendSignal()
        case e: STimerEvent =>
          e.getAndExecute()
        case sp: SSubProcess =>
          for {
            given ScenarioData <- sp.switchToSubProcess()
            given ScenarioData <- sp.runSteps()
            given ScenarioData <- sp.check()
            given ScenarioData <- sp.switchToMainProcess()
          } yield summon[ScenarioData]
        case SWaitTime(seconds) =>
          waitFor(seconds)
    end run

  end extension

  extension (hasProcessSteps: HasProcessSteps)
    def runSteps()(using
        data: ScenarioData
    ): ResultType =
      hasProcessSteps.steps.foldLeft[ResultType](Right(data)) {
        case (Right(data), step) =>
          given ScenarioData = data
          step.run()
        case (leftData, _) => leftData
      }

    def check()(using
        data: ScenarioData
    ): ResultType =
      for
        given ScenarioData <- checkFinished()(data)
        given ScenarioData <- checkVars()
      yield summon[ScenarioData]
    end check

    def checkVars()(using data: ScenarioData): ResultType =
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/history/variable-instance?processInstanceIdIn=$processInstanceId&deserializeValues=false"
      val request = basicRequest
        .auth()
        .get(uri)
      runRequest(request, s"Process '${hasProcessSteps.name}' checkVars")(
        (body, data) =>
          body
            .as[Seq[CamundaProperty]]
            .left
            .map(exc =>
              data
                .error(
                  s"!!! Problem parsing Result Body to a List of CamundaProperty."
                )
                .debug(s"Error: $exc")
                .debug(s"Response Body: $body")
            )
            .flatMap { value =>
              if (
                checkProps(
                  hasProcessSteps.asInstanceOf[WithTestOverrides[_]],
                  value
                )
              )
                Right(data.info("Variables successful checked"))
              else
                (
                  Left(
                    data.error(
                      "Variables do not match - see above in the Log (look for !!!)"
                    )
                  )
                )
            }
      )
    end checkVars

    def checkFinished()(data: ScenarioData): ResultType =
      val processInstanceId = data.context.processInstanceId
      val uri =
        uri"${config.endpoint}/history/process-instance/$processInstanceId"
      val request = basicRequest
        .auth()
        .get(uri)
      given ScenarioData = data
      runRequest(request, s"Process '${hasProcessSteps.name}' checkProcess") {
        (body, data) =>
          body.hcursor
            .downField("state")
            .as[String]
            .left
            .map { ex =>
              data
                .error(s"Problem extracting state from $body\n $ex")
            }
            .flatMap {
              /*
            COMPLETED - completed through normal end event
            EXTERNALLY_TERMINATED - terminated externally, for instance through REST API
            INTERNALLY_TERMINATED - terminated internally, for instance by terminating boundary event
               */
              case state
                  if state == "COMPLETED" || state.endsWith("_TERMINATED") =>
                Right(
                  data
                    .info(s"Process ${hasProcessSteps.name} has finished.")
                )
              case state =>
                given ScenarioData =
                  data.debug(s"State for ${hasProcessSteps.name} is $state")

                handleIncident()(summon[ScenarioData]) {
                  (body, data) =>
                    body.hcursor.values
                      .map {
                        case (values: Iterable[Json])
                            if values.toSeq.nonEmpty =>
                          extractIncidentMsg(body)(summon[ScenarioData])
                            .flatMap { case (incidentMessage, _, _) =>
                              Left(
                                data.error(
                                  s"There is a non-expected error occurred: ${incidentMessage
                                    .getOrElse("No incident message")}!"
                                )
                              )
                            }
                        case _ =>
                          Right(
                            summon[ScenarioData]
                              .debug(
                                s"No incident so far for ${hasProcessSteps.name}."
                              )
                          )
                      }
                      .getOrElse(
                        Left(
                          summon[ScenarioData].error(
                            "An Array is expected (should not happen)."
                          )
                        )
                      )
                }.flatMap(data =>
                  tryOrFail(checkFinished(), hasProcessSteps)(using data)
                )
            }
      }
    end checkFinished

    def handleIncident(
        rootIncidentId: Option[String] = None
    )(data: ScenarioData)(
        handleBody: (Json, ScenarioData) => ResultType
    ): ResultType =
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

      runRequest(request, s"Process '${hasProcessSteps.name}' checkIncident") {
        (body, data) => handleBody(body, data)
      }.left.map(_.info(request.toCurl))

    end handleIncident

    def extractIncidentMsg(body: Json)(
        data: ScenarioData
    ): Either[ScenarioData, (Option[String], String, String)] =
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
        }
    end extractIncidentMsg

  end extension

end SStepExtensions
