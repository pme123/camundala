package pme123.camundala.camunda.xml

import pme123.camundala.camunda.bpmn.ValidateWarnings
import pme123.camundala.model.ProcessTask

import scala.xml.{Elem, Node}

sealed trait XProcessTask
  extends XIdentifiableNode {
  def tagName: String

  def validate(maybeTask: Option[ProcessTask]): ValidateWarnings = maybeTask match {
    case None =>
      ValidateWarnings(s"There is NO a $tagName with id '$id' in your Process.")
    case Some(_) =>
      ValidateWarnings.none
  }

  def merge(maybeTask: Option[ProcessTask]): XMergeResult = (maybeTask, xmlNode) match {
    case (None, _) =>
      XMergeResult(xmlNode, ValidateWarnings(s"There is NO a $tagName with id '$id' in your Process."))
    case (Some(pt), taskElem: Elem) =>
      val propElem = taskElem \\ "property"
      val propParams = propElem.map(_ \@ "name")
      // xmlns:camunda={xmlnsCamunda} xmlns={xmlnsBpmn}>
      val xmlElem: Elem = taskElem.copy(
        child = <extensionElements>
          <camunda:properties>
            {for {(k, v) <- pt.extensions.properties
                  if !propParams.contains(k) // only add the one that not exist
                  } yield
              <camunda:property name={k} value={v}/>}{//
            propElem}
          </camunda:properties>
        </extensionElements>
      )
      XMergeResult(xmlElem, ValidateWarnings.none)
  }
}

case class XServiceTask(xmlNode: Node)
  extends XProcessTask {
  val tagName = "ServiceTask"
}

case class XUserTask(xmlNode: Node)
  extends XProcessTask {
  val tagName = "UserTask"

}

case class XMergeResult(xmlNode: Node, warnings: ValidateWarnings)
