package pme123.camundala.camunda.xml

import scala.xml.{Elem, Node, Null, PrefixedAttribute, Text, UnprefixedAttribute}

object XmlHelper {
  val xmlnsBpmn = "http://www.omg.org/spec/BPMN/20100524/MODEL"
  val xmlnsCamunda = "http://camunda.org/schema/1.0/bpmn"
  val xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance"
  val camundaPrefix = "camunda"

  val delegateExpression = "delegateExpression"
  val formKey = "formKey"

  def elementUnapply(n: Node, qual: XQualifier): Option[Elem] = n match {
    case e: Elem if XQualifier(e) == qual => Some(e)
    case _ => None
  }

  case class XQualifier(namespace: Option[String], label: String) {

    def extractFrom(e: Node, default: Option[String] = None): Option[String] =
      namespace.flatMap(e.attribute(_, label))
        .orElse(e.attribute(label))
        .flatMap(_.headOption)
        .fold(default)(x => Some(x.text))

    def mapAttr(e: Elem, mapper: String => String): Elem = {
      for {
        attr <- attributes(e)
        mappedAttr = attr.map {
          case Text(value) => Text(mapper(value))
          case other => other
        }
        if attr != mappedAttr
      } yield e % {
        namespace match {
          case Some(ns) =>
            val prefix = e.scope.getPrefix(ns)
            new PrefixedAttribute(prefix, label, mappedAttr, Null)
          case None => new UnprefixedAttribute(label, mappedAttr, Null)
        }
      }
    }.getOrElse(e)

    private def attributes(e: Node): Option[collection.Seq[Node]] = {
      namespace match {
        case Some(ns) => e.attribute(ns, label)
        case None =>
          e.attribute(label)
      }
    }
  }

  object XQualifier {

    def bpmn(label: String): XQualifier = XQualifier(xmlnsBpmn, label)

    def camunda(label: String): XQualifier = XQualifier(xmlnsCamunda, label)

    def apply(namespace: String, label: String): XQualifier = XQualifier(Some(namespace), label)

    def apply(n: Node): XQualifier = XQualifier(n.namespace, n.label)

    def apply(label: String): XQualifier = XQualifier(None, label)
  }

}
