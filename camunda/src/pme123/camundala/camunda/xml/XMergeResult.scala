package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.{Bpmn, BpmnId, FilePath}

import scala.xml.Elem

case class MergeResult(fileName: FilePath, xmlElem: Elem, maybeBpmn: Option[Bpmn], warnings: ValidateWarnings)

case class XMergeResult(xmlElem: Elem, warnings: ValidateWarnings)

