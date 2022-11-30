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

end SStepExtensions

