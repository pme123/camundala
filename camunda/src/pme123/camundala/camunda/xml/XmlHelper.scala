package pme123.camundala.camunda.xml

import scala.xml._

object XmlHelper {
  val xmlnsBpmn = "http://www.omg.org/spec/BPMN/20100524/MODEL"
  val xmlnsCamunda = "http://camunda.org/schema/1.0/bpmn"
  val xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance"
  val camundaPrefix = "camunda"
  val camundaXmlnsAttr: Attribute = Attribute("xmlns", camundaPrefix, xmlnsCamunda, Null)

  val delegateExpression = "delegateExpression"
  val javaClass = "class"
  val candidateStarterGroups = "candidateStarterGroups"
  val candidateStarterUsers = "candidateStarterUsers"
  val candidateGroups = "candidateGroups"
  val candidateUsers = "candidateUsers"
  val formKey = "formKey"

  def elementUnapply(n: Node, qual: QName): Option[Elem] = n match {
    case e: Elem if QName(e) == qual => Some(e)
    case _ => None
  }

  implicit class NodeExtension(elem: Node) {

    def attributeAsText(qName: QName): String =
      qAttributes(qName).toList.flatten.headOption
        .map(_.text)
        .getOrElse("")

    def qAttributes(qName: QName): Option[collection.Seq[Node]] =
      qName match {
        case QName(Some(ns), label) =>
          elem.attribute(ns, label)
        case QName(_, label) =>
          elem.attribute(label)
      }
  }

  implicit class ElemExtension(elem: Elem) {


    def mapAttr(qName: QName, mapper: String => String): Elem = {
      for {
        attr <- elem.qAttributes(qName)
        mappedAttr = attr.map {
          case Text(value) => Text(mapper(value))
          case other => other
        }
        if attr != mappedAttr
      } yield elem % {
        qName match {
          case QName(Some(ns), label) =>
            val prefix = elem.scope.getPrefix(ns)
            new PrefixedAttribute(prefix, label, mappedAttr, Null)
          case QName(_, label) =>
            new UnprefixedAttribute(label, mappedAttr, Null)
        }
      }
    }.getOrElse(elem)

  }

  case class
  QName(namespace: Option[String], label: String)

  object QName {

    def bpmn(label: String): QName = QName(xmlnsBpmn, label)

    def camunda(label: String): QName = QName(xmlnsCamunda, label)

    def apply(namespace: String, label: String): QName = QName(Some(namespace), label)

    def apply(n: Node): QName = QName(n.namespace, n.label)

    def apply(label: String): QName = QName(None, label)
  }

}
