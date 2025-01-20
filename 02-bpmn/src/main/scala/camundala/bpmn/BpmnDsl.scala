package camundala
package bpmn

trait BpmnDsl:
  def descr: String
  def companyDescr: String = ""

  private[bpmn] def msgNameDescr(messageName: String) =
    bpmnDescr(s"- _messageName_: `$messageName`")

  private[bpmn] def userTaskDescr(messageName: String) =
    bpmnDescr(s"- _taskDefinitionKey_: `$messageName`")

  private def bpmnDescr(keyLabel: String) =
    Some(s"""
            |$descr
            |
            |$keyLabel
            |
            |---
            |
            |$companyDescr
            |""".stripMargin)

end BpmnDsl
