package pme123.camundala.camunda.xml

import pme123.camundala.camunda.xml.XmlHelper._
import pme123.camundala.model.bpmn.ConditionExpression.{Expression, ExternalScript, InlineScript, JsonExpression}
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField.EnumField
import pme123.camundala.model.bpmn._
import zio.{Task, ZIO}

import scala.xml.{Attribute, Elem}

case class XBpmn(bpmnXml: Elem) {

  val processes: Seq[XBpmnProcess] =
    (bpmnXml \ "process")
      .filter(_ \@ "isExecutable" == "true")
      .map { case e: Elem => XBpmnProcess(e) }

  def merge(bpmn: Bpmn): Task[XMergeResult] = {
    val processWarnings =
      if (processes.length == bpmn.processes.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${processes.length} Processes in the XML-Model, but you have ${bpmn.processes.length} in Scala")
    for {
      mergeResults <- ZIO.foreach(processes)(xp => xp.idEffect.flatMap(id => xp.merge(bpmn.processMap.get(id))))
    } yield
      mergeResults
        .foldLeft(XMergeResult(bpmnXml, processWarnings)) {
          case (XMergeResult(resXml: Elem, resWarn), XMergeResult(procXml, procWarn)) =>
            XMergeResult(resXml.copy(child = resXml.child.map(c => if (c \@ "id" != procXml \@ "id") c else procXml)),
              resWarn ++ procWarn)
        }
  }

  def createProcesses(): Task[List[BpmnProcess]] =
    Task.foreach(processes)(_.createProcess())
}

case class XBpmnProcess(xmlElem: Elem) {

  val tagName: String = "Process"
  val idEffect: ZIO[Any, ModelException, ProcessId] = processIdFromStr(xmlElem \@ "id")

  val userTasks: Seq[XUserTask] =
    (xmlElem \ "userTask").map { case e: Elem => XUserTask(e) }

  val serviceTasks: Seq[XServiceTask[ServiceTask]] =
    (xmlElem \ "serviceTask").map { case e: Elem => XServiceTask(e) }

  val businessRuleTasks: Seq[XBusinessRuleTask[BusinessRuleTask]] =
    (xmlElem \ "businessRuleTask").map { case e: Elem => XBusinessRuleTask(e) }

  val sendTasks: Seq[XSendTask[SendTask]] =
    (xmlElem \ "sendTask").map { case e: Elem => XSendTask(e) }

  val startEvents: Seq[XStartEvent[StartEvent]] =
    (xmlElem \ "startEvent").map { case e: Elem => XStartEvent(e) }

  val exclusiveGateways: Seq[XExclusiveGateway[ExclusiveGateway]] =
    (xmlElem \ "exclusiveGateway").map { case e: Elem => XExclusiveGateway[ExclusiveGateway](e) }

  val parallelGateways: Seq[XParallelGateway[ParallelGateway]] =
    (xmlElem \ "parallelGateway").map { case e: Elem => XParallelGateway[ParallelGateway](e) }

  val sequenceFlows: Seq[XSequenceFlow[SequenceFlow]] =
    (xmlElem \ "sequenceFlow").map { case e: Elem => XSequenceFlow[SequenceFlow](e) }

  def merge(maybeProcess: Option[BpmnProcess]): Task[XMergeResult] =
    maybeProcess match {
      case None =>
        idEffect.map(id => XMergeResult(xmlElem, ValidateWarnings(s"There is no Process $id registered")))
      case Some(p) =>
        for {
          xmlCandidates <- mergeCandidates(xmlElem, p)
          XMergeResult(xmlUser, warningsUser) <- mergeExtensionable(xmlCandidates, p.userTaskMap, userTasks, "UserTask")
          XMergeResult(xmlService, warningsService) <- mergeExtensionable(xmlUser, p.serviceTaskMap, serviceTasks, "ServiceTask")
          XMergeResult(xmlStartEvent, warningsStartEvent) <- mergeExtensionable(xmlService, p.startEventMap, startEvents, "StartEvent")
          XMergeResult(xmlExclusiveGateway, warningsExclusiveGateway) <- mergeExtensionable(xmlStartEvent, p.exclusiveGatewayMap, exclusiveGateways, "ExclusiveGateway")
          XMergeResult(xmlParallelGateway, warningsParallelGateway) <- mergeExtensionable(xmlExclusiveGateway, p.parallelGatewayMap, parallelGateways, "ParallelGateway")
          XMergeResult(xmlSequenceFlow, warningsSequenceFlow) <- mergeExtensionable(xmlParallelGateway, p.sequenceFlowMap, sequenceFlows, "SequenceFlow")
          XMergeResult(xmlBusinessRuleService, warningsBusinessRule) <- mergeExtensionable(xmlSequenceFlow, p.businessRuleTaskMap, businessRuleTasks, "BusinessRuleTask")
        } yield
          XMergeResult(xmlBusinessRuleService, warningsUser ++ warningsService ++ warningsBusinessRule ++ warningsStartEvent ++ warningsExclusiveGateway ++ warningsParallelGateway ++ warningsSequenceFlow)
    }


  private def mergeCandidates(xml: Elem, process: BpmnProcess): Task[Elem] = Task {
    val starterGroups = process.starterGroups.asString(xmlElem.attributeAsText(QName.camunda(candidateStarterGroups)))
    val starterUsers = process.starterUsers.asString(xmlElem.attributeAsText(QName.camunda(candidateStarterUsers)))
    xml % Attribute(camundaPrefix, candidateStarterGroups, starterGroups,
      Attribute(camundaPrefix, candidateStarterUsers, starterUsers,
        camundaXmlnsAttr))
  }

  private def mergeExtensionable[A <: BpmnNode](xml: Elem, extensionableMap: Map[BpmnNodeId, A], xExts: Seq[XBpmnNode[A]], label: String): Task[XMergeResult] = {
    val warnings =
      if (extensionableMap.size == xExts.length)
        ValidateWarnings.none
      else
        ValidateWarnings(s"You have ${xExts.length} $label in the XML-Model, but you have ${extensionableMap.size} in Scala")

    for {
      mergeResults <- ZIO.foreach(xExts)(xn => xn.xBpmnId.flatMap(id => xn.merge(extensionableMap.get(id))))
    } yield
      mergeResults.foldLeft(XMergeResult(xml, warnings)) {
        case (XMergeResult(resXml: Elem, resWarn), XMergeResult(xml, taskWarn)) =>
          XMergeResult(resXml.copy(child = resXml.child.map(c => if (c \@ "id" == xml \@ "id") xml else c)),
            resWarn ++ taskWarn)
      }
  }

  def createProcess(): Task[BpmnProcess] =
    for {
      processId <- idEffect
      uTasks <- Task.foreach(userTasks)(_.create())
      sTasks <- Task.foreach(serviceTasks)(_.create())
      bRuleTasks <- Task.foreach(businessRuleTasks)(_.create())
      sendTasks <- Task.foreach(sendTasks)(_.create())
      startEvents <- Task.foreach(startEvents)(_.create())
      exGateways <- Task.foreach(exclusiveGateways)(_.create())
      pGateways <- Task.foreach(parallelGateways)(_.create())
      seqFlows <- Task.foreach(sequenceFlows)(_.create())
    } yield
      BpmnProcess(
        processId,
        CandidateGroups.none,
        CandidateUsers.none,
        uTasks,
        sTasks,
        bRuleTasks,
        sendTasks,
        startEvents,
        exGateways,
        pGateways,
        seqFlows
      )

}

trait XBpmnNode[T <: BpmnNode] {

  def xmlElem: Elem

  def tagName: String

  lazy val xBpmnId: ZIO[Any, ModelException, BpmnNodeId] = bpmnNodeIdFromStr(xmlElem \@ "id")

  def merge(maybeNode: Option[T]): Task[XMergeResult] = {
    xBpmnId.map(id => (maybeNode, xmlElem) match {
      case (None, _) =>
        XMergeResult(xmlElem, ValidateWarnings(s"There is NOT a $tagName with id '$id' in Scala."))
      case (Some(bpmnNode), nodeElem: Elem) =>

        val child = nodeElem.child
        val otherChild = child.filter(_.label != "extensionElements")
        val xmlElem: Elem =
          nodeElem.copy(
            child = <extensionElements xmlns:camunda={xmlnsCamunda} xmlns={xmlnsBpmn}>
              {bpmnNode match {
                case n: HasExtProperties => mergeHasProperties(n)
                case _ =>
              }}{bpmnNode match {
                case n: HasExtInOutputs => mergeHasInOutputs(n)
                case _ =>
              }}{bpmnNode match {
                case n: HasForm => mergeHasForm(n)
                case _ =>
              }}
            </extensionElements>
              ++ {
              otherChild
            }
          )
        XMergeResult(xmlElem, ValidateWarnings.none)
      case (Some(_), _) =>
        XMergeResult(xmlElem, ValidateWarnings(s"The XML Node must be a XML Elem not just a Node ($tagName with id '$id')."))
    }
    )
  }

  def mergeHasProperties(hasProps: HasExtProperties): Elem = {
    val propElems = xmlElem \ "extensionElements" \ "properties" \ "property"
    val propParams = propElems.map(_ \@ "name")

    <camunda:properties>
      {for {
      Prop(k, v) <- hasProps.extProperties.properties
      if !propParams.contains(k) // only add the one that not exist
    } yield
        <camunda:property name={k.value} value={v}/>}{//
      propElems}
    </camunda:properties>
  }

  def mergeHasInOutputs(hasInOuts: HasExtInOutputs): Elem = {
    val inputElems = xmlElem \\ "inputParameter"
    val inputParams = inputElems.map(_ \@ "name")
    val outputElems = xmlElem \\ "outputParameter"
    val outputParams = outputElems.map(_ \@ "name")

    <camunda:inputOutput>
      {for {
      (k, cond) <- hasInOuts.extInOutputs.inputMap
      if !inputParams.contains(k) // only add the one that not exist
    } yield
      <camunda:inputParameter name={k.value}>
        {expressionElem(cond)}
      </camunda:inputParameter>}{//
      inputElems}{//
      for {
        (k, cond) <- hasInOuts.extInOutputs.outputMap
        if !outputParams.contains(k) // only add the one that not exist
      } yield
        <camunda:outputParameter name={k.value}>
          {expressionElem(cond)}
        </camunda:outputParameter>}{//
      outputElems}
    </camunda:inputOutput>
  }

  def mergeHasForm(hasForm: HasForm): Elem = {
    val formFieldElems = xmlElem \\ "formField"
    val formFields = formFieldElems.map(_ \@ "id")

    <camunda:formData>
      {hasForm.maybeForm.toList.flatMap {
      case form: GeneratedForm => {
        form.allFields()
          .filter(ff => !formFields.contains(ff.id)) // only add if not defined
          .map(field =>
            createFormField(field)
          )
      }
      case _ => Seq.empty
    } ++ formFieldElems}
    </camunda:formData>
  }

  private def createFormField(field: FormField) = {
    <camunda:formField id={field.id} label={field.label} type={field.`type`.name} defaultValue={field.defaultValue}>
      {field match {
      case ef: EnumField =>
        ef.values.enums.map(ev => <camunda:value id={ev.key.value} name={ev.label}/>)
      case _ => {
      }
    }}<camunda:properties>
      {field.allProperties.map(p =>
          <camunda:property id={p.key.value} value={p.value}/>
      )}
    </camunda:properties>
      <camunda:validation>
        {field.validations.map(c =>
        c.config.map(config =>
            <camunda:constraint name={c.name.value} config={config}/>
        ).getOrElse(
            <camunda:constraint name={c.name.value}/>
        )
      )}
      </camunda:validation>
    </camunda:formField>
  }

  private

  def expressionElem
  (cond: ConditionExpression) = {
    cond match {
      case InlineScript(value, language) =>
        <camunda:script scriptFormat={language.key}>
          {value}
        </camunda:script>
      case ExternalScript(ref, language) =>
          <camunda:script scriptFormat={language.key} resource={s"deployment://${ref.fileName}"} />
      case jsonExpre: JsonExpression =>
        <camunda:script scriptFormat="Groovy">
          {jsonExpre.value}
        </camunda:script>
      case Expression(value) => value
    }
  }
}
