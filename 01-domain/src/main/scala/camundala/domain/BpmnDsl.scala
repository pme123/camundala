package camundala.domain

trait BpmnDsl:
  def descr: String
  def companyDescr: String = ""

  private[domain] def msgNameDescr(messageName: String) =
    bpmnDescr(s"- _messageName_: `$messageName`")

  private[domain] def userTaskDescr(messageName: String) =
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
