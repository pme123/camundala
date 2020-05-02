package pme123.camundala.camunda.xml

import pme123.camundala.model.bpmn.Bpmn

import scala.xml.Node

case class MergeResult(fileName:String, xmlNode: Node, maybeBpmn: Option[Bpmn], warnings: ValidateWarnings)

case class XMergeResult(xmlNode: Node, warnings: ValidateWarnings)

