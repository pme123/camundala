package camundala.examples.demos

import camundala.examples.demos.SignalMessageExample.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

// exampleDemos/GatlingIt/testOnly *SignalMessageExampleSimulation
// exampleDemos/It/testOnly *SignalMessageExampleSimulation
class SignalMessageExampleSimulation extends DemosSimulation:

  lazy val simulation = simulate {
    scenario(messageExample.startWithMsg)(
      messageIntermediateExample,
      messageIntermediateExample
        .waitFor("messageReady", true)
    )
    //TODO in doc:
    // .startWithSignal not supported as it is fire and forget
    // - but we need the processInstanceId as reference
    // 
    scenario(signalExample) (
      signalIntermediateExample
      .waitFor("signalReady", true),
    )

  }

  private lazy val messageIntermediateExample = receiveMessageEvent(
    "intermediate-message-for-example",
    in = SignalMessageExampleIn(),
  )

end SignalMessageExampleSimulation
