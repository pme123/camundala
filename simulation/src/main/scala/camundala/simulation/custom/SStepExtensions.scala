package camundala.simulation.custom

import camundala.simulation.*

trait SStepExtensions extends SUserTaskExtensions, SEventExtensions:

  extension (step: SStep) {
    def run()(using
        data: ScenarioData
    ): ResultType =
      step match {
        case ut: SUserTask =>
          ut.getAndComplete()
        case e: SReceiveMessageEvent =>
          e.sendMessage()
        case e: SReceiveSignalEvent =>
          e.sendSignal()
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
