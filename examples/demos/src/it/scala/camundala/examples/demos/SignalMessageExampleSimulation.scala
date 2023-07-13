package camundala.examples.demos

import camundala.examples.demos.SignalMessageExample.*
import camundala.simulation.*

// exampleDemos/It/testOnly *SignalMessageExampleSimulation
class SignalMessageExampleSimulation extends DemosSimulation:

  simulate {
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
