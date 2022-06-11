package camundala.api

import os.read.lines
import os.{Path, read}

import scala.xml.*
import scala.annotation.tailrec
import java.io.{StringBufferInputStream, StringReader}

/** Checks all BPMNs if a process is used in another process. As result a list
  * is created that can be included in the Documentation.
  */
trait ProcessReferenceCreator:

  def projectName: String

  def docUsedByReference(processName: String): String =
    val refs = findBpmnFor(processName)
    val refDoc = refs
      .map { case k -> processes =>
        s"""_${k}_
           |${processes.map(_._2).mkString("   - ", "\n   - ", "\n")}
           |""".stripMargin
      }
      .mkString("\n- ", "\n- ", "\n")
    println(refDoc)
    if(refDoc.trim.length == 1)
      "\n**Used in no other Process.**\n"
    else  
      s"""
        |<details>
        |<summary><b>${usedByTitle(refs.size)}</b></summary>
        |<p>
        |
        |$refDoc
        |
        |</p>
        |</details>
        |""".stripMargin

  class UsesRef(processRef: String, serviceName: Option[String]):
    lazy val (project: String, processId: String) =
      processRef.split(":").toList match
        case proj :: proc :: _ => (proj, proc)
        case proc :: _ => (projectName, proc)
        case Nil =>
          throw new IllegalArgumentException(
            "There must be at least a processId defined."
          )

    lazy val processIdent = serviceName.getOrElse(processId)
    lazy val anchor = serviceName
      .map(_ => s"#operation/Process:%20$processIdent")
      .getOrElse(s"#tag/$processIdent")
    lazy val serviceStr = serviceName.map(_ => s" ($processId)").getOrElse("")

    lazy val asString =
      s"_[$processIdent](${docProjectUrl(project)}/OpenApi.html$anchor)_ $serviceStr"

  def docUsesReference(processName: String): String =
    findBpmn(processName)
      .map { xmlStr =>
        val xml = XML.load(new StringReader(xmlStr))
        val callActivities = xml \\ "callActivity"

        val refs = callActivities
          .map { ca =>
            val calledElement = ca \@ "calledElement"
            val maybeServiceName = (ca \\ "in")
              .filter(_ \@ "target" == "serviceName")
              .map(_ \@ "sourceExpression")
              .headOption
            UsesRef(calledElement, maybeServiceName)
          }
          .groupBy(_.project)
          .toSeq
          .sortBy(_._1)

        val refDoc = refs
          .map { case k -> processes =>
            s"""_${k}_
              |${processes
              .map(_.asString)
              .toSeq
              .distinct
              .sorted
              .mkString("   - ", "\n   - ", "\n")}
              |""".stripMargin
          }
          .mkString("\n- ", "\n- ", "\n")
        if(refDoc.trim.length == 1)
          "\n**Uses no other Processes.**\n"
        else  
          s"""
            |<details>
            |<summary><b>${usesTitle(refs.size)}</b></summary>
            |<p>
            |
            |$refDoc
            |</p>
            |</details>
            |""".stripMargin
      }
      .getOrElse("\n**Uses no other Processes.**\n")

  protected def usedByTitle(processCount: Int): String =
    s"Used in $processCount Project(s) (EXPERIMENTAL)"

  protected def usesTitle(processCount: Int): String =
    s"Uses $processCount Project(s) (EXPERIMENTAL)"

  protected def docProjectUrl(project: String): String

  protected def projectPaths: Seq[Path] =
    Seq(
      os.pwd / os.up
    )

  protected def docuPath(
      projectPath: Path,
      path: Path,
      content: String
  ): (String, String) =
    val extractId =
      val pattern =
        """<(bpmn:process|process)([^\/>]+)isExecutable="true"([^\/>]*>)""".r
      val idPattern = """[\s\S]*id="([^"]*)"[\s\S]*""".r
      pattern
        .findFirstIn(content)
        .map { l =>
          val idPattern(id) = l
          id
        }
        .getOrElse(s"Id not found in $path")

    val extractId2 =
      val pattern = ".*id=\"([^\"]*)\".*".r
      lines(path).toList
        .dropWhile(!_.matches(".*<(bpmn:process|process).*"))
        .find(_.contains("id="))
        .map { l =>
          val pattern(id) = l
          id
        }
        .mkString

    @tailrec
    def projectName(segments: List[String]): (String, String) =
      segments match
        case projectName :: y :: _ if y == projectPath.last =>
          projectName -> s"[$extractId](${docProjectUrl(projectName)}/OpenApi.html#tag/$extractId)"
        case _ :: xs => projectName(xs)
        case Nil => "NOT_DEFINED" -> "projectName could not be extracted"

    projectName(path.segments.toList.reverse)

  protected lazy val allBpmns =
    projectPaths
      .map { p =>
        println(s"Get BPMNs in $p")
        p ->
          (if (os.exists(p)) os.walk(p)
           else {
             println(s"THIS PATH DOES NOT EXIST: $p")
             Seq.empty
           })
      }
      .map { case projectPath -> path =>
        projectPath -> path
          .filterNot(_.toString.contains("/target"))
          .filter(_.toString.endsWith(".bpmn"))
          .map(p => p -> read(p))
      }

  protected def findBpmnFor(
      processName: String
  ): Seq[(String, Seq[(String, String)])] =
    println(s"Find References for $processName")
    allBpmns
      .flatMap { case (pp, paths) =>
        paths
          .filter { case p -> c =>
            c.matches(s"""[\\s\\S]*(:|")$processName"[\\s\\S]*""") &&
              !c.contains(s"id=\"$processName\"")
          }
          .map(pc => docuPath(pp, pc._1, pc._2))
      }
      .groupBy(_._1)
      .toSeq
      .sortBy(_._1)

  protected def findBpmn(
      processName: String
  ): Option[String] =
    println(s"Find BPMN for $processName")
    allBpmns.flatMap { case _ -> paths =>
      paths
        .filter { case _ -> content =>
          content.contains(s"id=\"$processName\"")
        }
        .map(_._2)
    }.headOption

object XMLChecker extends App:
  def docProjectUrl(project: String): String =
    s"https://bpf.apps.grv.scbs.ch/$project"

  class UsesRef(processRef: String, serviceName: Option[String]):
    lazy val (project: String, processId: String) =
      processRef.split(":").toList match
        case proj :: proc :: _ => (proj, proc)
        case proc :: _ => (projectName, proc)
        case Nil =>
          throw new IllegalArgumentException(
            "There must be at least a processId defined."
          )

    lazy val processIdent = serviceName.getOrElse(processId)
    lazy val anchor = serviceName
      .map(_ => s"#operation/Process:%20$processIdent")
      .getOrElse(s"#tag/$processIdent")
    lazy val serviceStr = serviceName.map(_ => s" ($processId)").getOrElse("")

    lazy val asString =
      s"_[$processIdent](${docProjectUrl(project)}/OpenApi.html$anchor)_ $serviceStr"

  val projectName = "valiant-bpmn"
  val xml = XML.load(new StringBufferInputStream(xmlStr))
  val callActivities = xml \\ "callActivity"

  val refs = callActivities
    .map { ca =>
      val calledElement = ca \@ "calledElement"
      val maybeServiceName = (ca \\ "in")
        .filter(_ \@ "target" == "serviceName")
        .map(_ \@ "sourceExpression")
        .headOption
      UsesRef(calledElement, maybeServiceName)
    }
    .groupBy(_.project)
    .toSeq
    .sortBy(_._1)

  val refDoc = refs
    .map { case k -> processes =>
      s"""_${k}_
         |${processes
        .map(_.asString)
        .toSeq
        .distinct
        .sorted
        .mkString("   - ", "\n   - ", "\n")}
         |""".stripMargin
    }
    .mkString("\n- ", "\n- ", "\n")

  os.write.over(os.pwd / "REFS.md", refDoc)

  lazy val xmlStr = """<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:camunda="http://camunda.org/schema/1.0/bpmn" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:bioc="http://bpmn.io/schema/bpmn/biocolor/1.0" xmlns:color="http://www.omg.org/spec/BPMN/non-normative/color/1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:modeler="http://camunda.org/schema/modeler/1.0" id="Definitions_19vzwcd" targetNamespace="http://bpmn.io/schema/bpmn" exporter="Camunda Modeler" exporterVersion="4.11.1" camunda:diagramRelationId="b9ddc512-7731-4480-9249-b4cb100fe129" modeler:executionPlatform="Camunda Platform" modeler:executionPlatformVersion="7.15.0">
  <bpmn:collaboration id="Collaboration_1i6g0i7">
    <bpmn:participant id="Participant_valiant-bpmn-contract-createUpdateForPartner" name="bpmn-contract-createUpdateForPartner" processRef="valiant-bpmn-contract-createUpdateForPartner" />
  </bpmn:collaboration>
  <bpmn:process id="valiant-bpmn-contract-createUpdateForPartner" name="valiant-bpmn-contract-createUpdateForPartner" isExecutable="true" camunda:candidateStarterGroups="admin">
    <bpmn:endEvent id="Event_0xa059b" name="Contracts created or updated">
      <bpmn:extensionElements>
        <camunda:executionListener expression="${execution.setVariable(&#34;endStatus&#34;, &#34;createdUpdated&#34;)}" event="start" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_03qu1zw</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:callActivity id="Activity_1mb1vpo" name="create / Update Contract clientKey" camunda:asyncAfter="true" calledElement="valiant-bpmn-contract-createUpdate" camunda:calledElementBinding="deployment">
      <bpmn:extensionElements>
        <camunda:in source="clientKey" target="clientKey" />
        <camunda:in source="contract" target="contract" />
        <camunda:in source="impersonateUserId" target="impersonateUserId" />
        <camunda:in source="contractStatus" target="newContractStatus" />
        <camunda:out source="contractKey" target="contractKey" />
        <camunda:in source="contractType" target="contractType" />
        <camunda:out source="endStatus" target="createUpdateEndStatus" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0yuqfyb</bpmn:incoming>
      <bpmn:outgoing>Flow_03qu1zw</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:exclusiveGateway id="Gateway_0ae8azx" name="contractExists for both clientKeys?" default="Flow_0xxf4t2">
      <bpmn:incoming>Flow_0ke066a</bpmn:incoming>
      <bpmn:outgoing>Flow_0xxf4t2</bpmn:outgoing>
      <bpmn:outgoing>Flow_09fw6i2</bpmn:outgoing>
    </bpmn:exclusiveGateway>
    <bpmn:callActivity id="Activity_0ko89tm" name="get Contract for relationClientKeys" calledElement="finnova-fil-is:finnova-fil-is-genericGet" camunda:calledElementBinding="deployment">
      <bpmn:extensionElements>
        <camunda:in source="toClientKey" target="clientKey" />
        <camunda:out source="contracts" target="contractsForClientKey" />
        <camunda:in sourceExpression="contract.api.v2.get" target="serviceName" />
        <camunda:in source="impersonateUserId" target="impersonateUserId" />
        <camunda:executionListener event="end">
          <camunda:script scriptFormat="groovy" resource="scripts/contract/createUpdateForPartner/mergeContracts.groovy" />
        </camunda:executionListener>
        <camunda:in source="contractStatus" target="contractStatus" />
        <camunda:in source="contractType" target="contractType" />
        <camunda:in source="contractsExistForToClientKeysMock" target="customMock" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_1094x1f</bpmn:incoming>
      <bpmn:outgoing>Flow_0ke066a</bpmn:outgoing>
      <bpmn:multiInstanceLoopCharacteristics isSequential="true" camunda:asyncAfter="true" camunda:collection="${relationClientKeys.elements()}" camunda:elementVariable="toClientKey" />
    </bpmn:callActivity>
    <bpmn:endEvent id="Event_1trumm6" name="Contracts skipped">
      <bpmn:extensionElements>
        <camunda:executionListener expression="${execution.setVariable(&#34;endStatus&#34;, &#34;skipped&#34;)}" event="start" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_09fw6i2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1094x1f" sourceRef="StartEvent_1" targetRef="Activity_0ko89tm" />
    <bpmn:sequenceFlow id="Flow_0ke066a" sourceRef="Activity_0ko89tm" targetRef="Gateway_0ae8azx" />
    <bpmn:sequenceFlow id="Flow_0xxf4t2" name="yes" sourceRef="Gateway_0ae8azx" targetRef="Activity_0w8buzd" />
    <bpmn:sequenceFlow id="Flow_0yuqfyb" sourceRef="Activity_0w8buzd" targetRef="Activity_1mb1vpo" />
    <bpmn:sequenceFlow id="Flow_03qu1zw" sourceRef="Activity_1mb1vpo" targetRef="Event_0xa059b" />
    <bpmn:sequenceFlow id="Flow_09fw6i2" name="no" sourceRef="Gateway_0ae8azx" targetRef="Event_1trumm6">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${!contractsExists}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>
    <bpmn:startEvent id="StartEvent_1" name="create / update Contract">
      <bpmn:extensionElements>
        <camunda:executionListener event="start">
          <camunda:script scriptFormat="groovy" resource="scripts/contract/createUpdateForPartner/initContractTypeAndStatus.groovy" />
        </camunda:executionListener>
      </bpmn:extensionElements>
      <bpmn:outgoing>Flow_1094x1f</bpmn:outgoing>
      <bpmn:messageEventDefinition id="MessageEventDefinition_1upeynm" messageRef="Message_12zu745" />
    </bpmn:startEvent>
    <bpmn:callActivity id="Activity_0w8buzd" name="getContract clientKey" camunda:asyncAfter="true" calledElement="finnova-fil-is:finnova-fil-is-genericGet" camunda:calledElementBinding="deployment">
      <bpmn:extensionElements>
        <camunda:in sourceExpression="contract.api.v2.get" target="serviceName" />
        <camunda:in source="clientKey" target="clientKey" />
        <camunda:in source="contractType" target="contractType" />
        <camunda:out source="contract" target="contract" />
        <camunda:in source="impersonateUserId" target="impersonateUserId" />
        <camunda:in source="contractMock" target="customMock" />
      </bpmn:extensionElements>
      <bpmn:incoming>Flow_0xxf4t2</bpmn:incoming>
      <bpmn:outgoing>Flow_0yuqfyb</bpmn:outgoing>
    </bpmn:callActivity>
    <bpmn:textAnnotation id="TextAnnotation_0vs2dfz">
      <bpmn:text>contractStatus contractType</bpmn:text>
    </bpmn:textAnnotation>
    <bpmn:association id="Association_1iowul1" sourceRef="Activity_0ko89tm" targetRef="TextAnnotation_0vs2dfz" />
  </bpmn:process>
  <bpmn:message id="Message_12zu745" name="valiant-bpmn-contract-createUpdateForPartner" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Collaboration_1i6g0i7">
      <bpmndi:BPMNShape id="Participant_0ztnw9u_di" bpmnElement="Participant_valiant-bpmn-contract-createUpdateForPartner" isHorizontal="true">
        <dc:Bounds x="161" y="85" width="979" height="343" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_09fw6i2_di" bpmnElement="Flow_09fw6i2">
        <di:waypoint x="530" y="265" />
        <di:waypoint x="530" y="350" />
        <di:waypoint x="1042" y="350" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="539" y="305" width="13" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_03qu1zw_di" bpmnElement="Flow_03qu1zw">
        <di:waypoint x="970" y="240" />
        <di:waypoint x="1042" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0yuqfyb_di" bpmnElement="Flow_0yuqfyb">
        <di:waypoint x="780" y="240" />
        <di:waypoint x="870" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0xxf4t2_di" bpmnElement="Flow_0xxf4t2">
        <di:waypoint x="555" y="240" />
        <di:waypoint x="680" y="240" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="561" y="248" width="18" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_0ke066a_di" bpmnElement="Flow_0ke066a">
        <di:waypoint x="410" y="240" />
        <di:waypoint x="505" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_1094x1f_di" bpmnElement="Flow_1094x1f">
        <di:waypoint x="248" y="240" />
        <di:waypoint x="310" y="240" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNShape id="Event_0xa059b_di" bpmnElement="Event_0xa059b">
        <dc:Bounds x="1042" y="222" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1017" y="265" width="87" height="27" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_1mb1vpo_di" bpmnElement="Activity_1mb1vpo" bioc:fill="#ffffff" color:background-color="#ffffff">
        <dc:Bounds x="870" y="200" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Gateway_0ae8azx_di" bpmnElement="Gateway_0ae8azx" isMarkerVisible="true">
        <dc:Bounds x="505" y="215" width="50" height="50" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="537" y="186" width="85" height="27" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0ko89tm_di" bpmnElement="Activity_0ko89tm" bioc:stroke="#000000" bioc:fill="#c5ffd7" color:background-color="#c5ffd7" color:border-color="#000000">
        <dc:Bounds x="310" y="200" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1trumm6_di" bpmnElement="Event_1trumm6">
        <dc:Bounds x="1042" y="332" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="1016" y="375" width="89" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Event_1hxrgmq_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="212" y="222" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="193" y="265" width="74" height="27" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Activity_0w8buzd_di" bpmnElement="Activity_0w8buzd" bioc:fill="#c6ffda" color:background-color="#c6ffda">
        <dc:Bounds x="680" y="200" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="TextAnnotation_0vs2dfz_di" bpmnElement="TextAnnotation_0vs2dfz">
        <dc:Bounds x="350" y="140" width="100" height="40" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Association_1iowul1_di" bpmnElement="Association_1iowul1">
        <di:waypoint x="360" y="200" />
        <di:waypoint x="398" y="180" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>
    """
