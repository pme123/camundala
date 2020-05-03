package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.Bpmn

import scala.xml.{Elem, Node}

case class MergeResult(fileName:String, xmlElem: Elem, maybeBpmn: Option[Bpmn], warnings: ValidateWarnings)

case class XMergeResult(xmlElem: Elem, warnings: ValidateWarnings)

