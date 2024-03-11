package camundala.examples.demos.simulation

import camundala.examples.demos.bpmn.MessageForExample.messageExample
import camundala.examples.demos.bpmn.SignalExample.*
import camundala.examples.demos.bpmn.SignalMessageExampleIn
import camundala.simulation.*

// exampleDemosSimulation/test
// exampleDemosSimulation/testOnly *SignalMessageExampleSimulation
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
