package camundala.simulation.custom

import camundala.simulation.*

trait SStepExtensions extends SUserTaskExtensions :

  extension (step: SStep) {
    def run()(using
              data: ScenarioData
    ): ResultType =
      step match {
        case ut: SUserTask =>
          ut.getAndComplete()
        case e: SReceiveMessageEvent =>
          Left(data.error(s"SReceiveMessageEvent is not implemented"))
        // e.correlate(config.tenantId)
        case e: SReceiveSignalEvent =>
          Left(data.error(s"SReceiveSignalEvent is not implemented"))
        // e.sendSignal()
        case sp: SSubProcess =>
          Left(data.error(s"SSubProcess is not implemented"))
        /*  sp.switchToSubProcess() ++
             sp.steps.flatMap(toGatling) ++
             sp.check() :+
             sp.switchToMainProcess() */
        case SWaitTime(seconds) =>
          Left(data.error(s"SWaitTime is not implemented"))
        //  Seq(exec().pause(seconds))

      }
  }

end SStepExtensions

