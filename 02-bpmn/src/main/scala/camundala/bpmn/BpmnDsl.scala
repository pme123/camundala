package camundala
package bpmn

import camundala.domain.*

import scala.reflect.ClassTag

trait BpmnDsl:
  def descr: String
  def companyDescr: String

  private[bpmn] def msgNameDescr(messageName: String) =
    Some(s"""
       |$descr
       |
       |- _messageName_: `$messageName`
       |
       |---
       |
       |$companyDescr
       |""".stripMargin)
    
  private[bpmn] lazy val defaultDescr =
    Some(s"""
       |$descr
       |
       |---
       |
       |$companyDescr
       |""".stripMargin)

end BpmnDsl
