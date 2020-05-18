
import eu.timepit.refined.auto._
import pme123.camundala.camunda.delegate.RestServiceDelegate
import pme123.camundala.examples.common.deploys
import pme123.camundala.model.bpmn._


val bpmns: Set[Bpmn] =
  Set(
    Bpmn("Playground.bpmn",
      StaticFile("Playground.bpmn", "bpmn"),
      List(
        BpmnProcess("PlaygroundProcess",
          userTasks = List(UserTask("ShowResultTask")),
          serviceTasks = List(
            ServiceTask("CallSwapiServiceTask",
              RestServiceDelegate.expression
            )),
          sendTasks = List(),
          startEvents = List(StartEvent("DefineInputsStartEvent")),
          exclusiveGateways = List(),
          parallelGateways = List(),
          sequenceFlows = List(SequenceFlow("SequenceFlow_9"), SequenceFlow("SequenceFlow_0m72fzi"), SequenceFlow("SequenceFlow_0k5kyka")),
        )))
  )

deploys.standard(bpmns)