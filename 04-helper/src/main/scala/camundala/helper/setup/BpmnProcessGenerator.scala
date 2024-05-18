package camundala.helper.setup

case class BpmnProcessGenerator()(using config: SetupConfig):

  def createBpmn(setupElement: SetupElement): Unit =
    val name = s"${setupElement.identifierShort}.bpmn"
    os.write.over(
      bpmnPath / name,
      bpmn(setupElement)
    )
  end createBpmn

  private lazy val bpmnPath =
    val dir = config.projectDir / config.bpmnPath
    os.makeDir.all(dir)
    dir
  end bpmnPath

  private def bpmn(setupElement: SetupElement) =
    val processId = setupElement.identifier
    s"""<?xml version="1.0" encoding="UTF-8"?>
       |<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:bioc="http://bpmn.io/schema/bpmn/biocolor/1.0" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:modeler="http://camunda.org/schema/modeler/1.0" id="Definitions_0phxlok" targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="5.19.0" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.20.0">
       |  <bpmn:collaboration id="Collaboration_1nchi5w">
       |    <bpmn:participant id="$processId-Participant" name="$processId" processRef="$processId" />
       |  </bpmn:collaboration>
       |  <bpmn:process id="$processId" name="$processId" isExecutable="true" camunda:historyTimeToLive="180">
       |    <bpmn:startEvent id="ProcessStartEvent" name="Start Process" camunda:asyncAfter="true">
       |      <bpmn:outgoing>Flow_1jqb7g9</bpmn:outgoing>
       |      <bpmn:messageEventDefinition id="MessageEventDefinition_0uhyvc8" messageRef="Message_0q4quzr" />
       |    </bpmn:startEvent>
       |    <bpmn:serviceTask id="InitProcessTask" name="Init Process" camunda:type="external" camunda:topic="$processId">
       |      <bpmn:extensionElements>
       |        <camunda:inputOutput>
       |          <camunda:inputParameter name="handledErrors">output-mocked, validation-failed</camunda:inputParameter>
       |        </camunda:inputOutput>
       |      </bpmn:extensionElements>
       |      <bpmn:incoming>Flow_1jqb7g9</bpmn:incoming>
       |      <bpmn:outgoing>Flow_151h2k5</bpmn:outgoing>
       |    </bpmn:serviceTask>
       |    <bpmn:intermediateThrowEvent id="ValidationfailedEvent1" name="validation-failed">
       |      <bpmn:incoming>Flow_1szk79c</bpmn:incoming>
       |      <bpmn:linkEventDefinition id="LinkEventDefinition_0x9gj38" name="validation-failed" />
       |    </bpmn:intermediateThrowEvent>
       |    <bpmn:intermediateThrowEvent id="OutputmockedEvent1" name="output-mocked">
       |      <bpmn:incoming>Flow_1tnbvod</bpmn:incoming>
       |      <bpmn:linkEventDefinition id="LinkEventDefinition_147ix62" name="output-mocked" />
       |    </bpmn:intermediateThrowEvent>
       |    <bpmn:endEvent id="SucceededEndEvent" name="succeeded">
       |      <bpmn:extensionElements>
       |        <camunda:executionListener expression="#{execution.setVariable(&#34;processStatus&#34;, &#34;succeeded&#34;)}" event="start" />
       |      </bpmn:extensionElements>
       |      <bpmn:incoming>Flow_151h2k5</bpmn:incoming>
       |    </bpmn:endEvent>
       |    <bpmn:endEvent id="OutputmockedEndEvent" name="output-mocked">
       |      <bpmn:extensionElements>
       |        <camunda:executionListener expression="#{execution.setVariable(&#34;processStatus&#34;, &#34;output-mocked&#34;)}" event="start" />
       |      </bpmn:extensionElements>
       |      <bpmn:incoming>Flow_0vdwlr8</bpmn:incoming>
       |    </bpmn:endEvent>
       |    <bpmn:endEvent id="ValidationfailedEndEvent" name="validation-failed">
       |      <bpmn:extensionElements>
       |        <camunda:executionListener expression="#{execution.setVariable(&#34;processStatus&#34;, &#34;notValid&#34;)}" event="start" />
       |      </bpmn:extensionElements>
       |      <bpmn:incoming>Flow_1canyvf</bpmn:incoming>
       |      <bpmn:errorEventDefinition id="ErrorEventDefinition_00goxx6" errorRef="Error_0tdcv3y" />
       |    </bpmn:endEvent>
       |    <bpmn:intermediateCatchEvent id="ValidationfailedEvent" name="validation-failed">
       |      <bpmn:outgoing>Flow_1canyvf</bpmn:outgoing>
       |      <bpmn:linkEventDefinition id="LinkEventDefinition_1kij05c" name="validation-failed" />
       |    </bpmn:intermediateCatchEvent>
       |    <bpmn:intermediateCatchEvent id="OutputmockedEvent" name="output-mocked">
       |      <bpmn:outgoing>Flow_0vdwlr8</bpmn:outgoing>
       |      <bpmn:linkEventDefinition id="LinkEventDefinition_17cwbts" name="output-mocked" />
       |    </bpmn:intermediateCatchEvent>
       |    <bpmn:boundaryEvent id="MockedBoundaryEvent" attachedToRef="InitProcessTask">
       |      <bpmn:outgoing>Flow_1tnbvod</bpmn:outgoing>
       |      <bpmn:errorEventDefinition id="ErrorEventDefinition_11vypoq" errorRef="Error_1rvzdyr" />
       |    </bpmn:boundaryEvent>
       |    <bpmn:boundaryEvent id="Event_1roicyw" attachedToRef="InitProcessTask">
       |      <bpmn:outgoing>Flow_1szk79c</bpmn:outgoing>
       |      <bpmn:errorEventDefinition id="ErrorEventDefinition_0ogxnq9" errorRef="Error_1k095oq" />
       |    </bpmn:boundaryEvent>
       |    <bpmn:sequenceFlow id="Flow_1jqb7g9" sourceRef="PrintDocumentStartEvent" targetRef="InitProcessTask" />
       |    <bpmn:sequenceFlow id="Flow_151h2k5" sourceRef="InitProcessTask" targetRef="RubrikUpdatedEndEvent" />
       |    <bpmn:sequenceFlow id="Flow_1szk79c" sourceRef="Event_1roicyw" targetRef="ValidationfailedEvent1" />
       |    <bpmn:sequenceFlow id="Flow_1tnbvod" sourceRef="MockedBoundaryEvent" targetRef="OutputmockedEvent1" />
       |    <bpmn:sequenceFlow id="Flow_0vdwlr8" sourceRef="OutputmockedEvent" targetRef="OutputmockedEndEvent" />
       |    <bpmn:sequenceFlow id="Flow_1canyvf" sourceRef="ValidationfailedEvent" targetRef="ValidationfailedEndEvent" />
       |  </bpmn:process>
       |  <bpmn:message id="Message_0q4quzr" name="$processId" />
       |  <bpmn:error id="Error_0tdcv3y" name="validation-failed" errorCode="validation-failed" camunda:errorMessage="#{validationErrors.toString()}" />
       |  <bpmn:error id="Error_1rvzdyr" name="output-mocked" errorCode="output-mocked" />
       |  <bpmn:error id="Error_1k095oq" name="validation-failed" errorCode="validation-failed" camunda:errorMessage="#{validationErrors.toString()}" />
       |  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
       |    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1nchi5w">
       |      <bpmndi:BPMNShape id="Participant_1wuaud6_di" bpmnElement="$processId-Participant" isHorizontal="true">
       |        <dc:Bounds x="160" y="80" width="1450" height="630" />
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="Event_0ucqip2_di" bpmnElement="PrintDocumentStartEvent">
       |        <dc:Bounds x="222" y="279" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="202" y="322" width="76" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="Activity_1luk2dc_di" bpmnElement="InitProcessTask" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="310" y="257" width="100" height="80" />
       |        <bpmndi:BPMNLabel />
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_0uq59s2" bpmnElement="ValidationfailedEvent1" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="292" y="399" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="272" y="442" width="77" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="Event_084c0k8_di" bpmnElement="OutputmockedEvent1" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="392" y="399" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="374" y="442" width="73" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="Event_1vr44rw_di" bpmnElement="RubrikUpdatedEndEvent">
       |        <dc:Bounds x="1502" y="279" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="1483" y="322" width="75" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_0leg8xy" bpmnElement="OutputmockedEndEvent" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="1480" y="489" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="1461" y="535" width="73" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_0c5az63" bpmnElement="ValidationfailedEndEvent" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="1480" y="569" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="1459" y="615" width="77" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_19yz9z9" bpmnElement="ValidationfailedEvent" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="1362" y="569" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="1344" y="612" width="77" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_1km6s3c" bpmnElement="OutputmockedEvent" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="1362" y="489" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="1346" y="532" width="73" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="Event_047id8n_di" bpmnElement="MockedBoundaryEvent" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="392" y="319" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="453" y="193" width="73" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNShape id="BPMNShape_1oeltv5" bpmnElement="Event_1roicyw" bioc:fill="#D5D5D5">
       |        <dc:Bounds x="292" y="319" width="36" height="36" />
       |        <bpmndi:BPMNLabel>
       |          <dc:Bounds x="252.5" y="193" width="39" height="14" />
       |        </bpmndi:BPMNLabel>
       |      </bpmndi:BPMNShape>
       |      <bpmndi:BPMNEdge id="Flow_1jqb7g9_di" bpmnElement="Flow_1jqb7g9">
       |        <di:waypoint x="258" y="297" />
       |        <di:waypoint x="310" y="297" />
       |      </bpmndi:BPMNEdge>
       |      <bpmndi:BPMNEdge id="Flow_151h2k5_di" bpmnElement="Flow_151h2k5">
       |        <di:waypoint x="410" y="297" />
       |        <di:waypoint x="1502" y="297" />
       |      </bpmndi:BPMNEdge>
       |      <bpmndi:BPMNEdge id="Flow_1szk79c_di" bpmnElement="Flow_1szk79c">
       |        <di:waypoint x="310" y="355" />
       |        <di:waypoint x="310" y="399" />
       |      </bpmndi:BPMNEdge>
       |      <bpmndi:BPMNEdge id="Flow_1tnbvod_di" bpmnElement="Flow_1tnbvod">
       |        <di:waypoint x="410" y="355" />
       |        <di:waypoint x="410" y="399" />
       |      </bpmndi:BPMNEdge>
       |      <bpmndi:BPMNEdge id="BPMNEdge_0a6fs7m" bpmnElement="Flow_0vdwlr8">
       |        <di:waypoint x="1398" y="507" />
       |        <di:waypoint x="1480" y="507" />
       |      </bpmndi:BPMNEdge>
       |      <bpmndi:BPMNEdge id="BPMNEdge_0mo5gwm" bpmnElement="Flow_1canyvf">
       |        <di:waypoint x="1398" y="587" />
       |        <di:waypoint x="1480" y="587" />
       |      </bpmndi:BPMNEdge>
       |    </bpmndi:BPMNPlane>
       |  </bpmndi:BPMNDiagram>
       |</bpmn:definitions>
       |""".stripMargin
end BpmnProcessGenerator
