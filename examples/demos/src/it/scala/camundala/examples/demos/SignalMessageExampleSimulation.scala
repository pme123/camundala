package camundala.examples.demos

import camundala.examples.demos.SignalMessageExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/GatlingIt/testOnly *SignalMessageExampleSimulation
// exampleDemos/It/testOnly *SignalMessageExampleSimulation
class SignalMessageExampleSimulation extends CustomSimulation:

  lazy val simulation: LogLevel = simulate {
    scenario(messageExample.startWithMsg)
    //TODO in doc:
    // .startWithSignal not supported as it is fire and forget 
    // - but we need the processInstanceId as reference
    scenario(signalExample) 


  }

  override implicit def config =
    super.config.withPort(8033)

end SignalMessageExampleSimulation
