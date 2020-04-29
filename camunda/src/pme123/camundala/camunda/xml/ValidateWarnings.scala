package pme123.camundala.camunda.xml

import org.camunda.bpm.model.bpmn.{BpmnModelInstance, instance => camunda}

case class MergeProcess(process: camunda.Process, warnings: ValidateWarnings)
case class MergeTask(task: camunda.Task, warnings: ValidateWarnings)

case class ValidateWarnings(value: Seq[ValidateWarning]) {

  def ++(other: ValidateWarnings): ValidateWarnings =
    ValidateWarnings(value ++ other.value)
}

object ValidateWarnings {
  def none: ValidateWarnings = ValidateWarnings(Seq.empty)

  def apply(warnMsg: String): ValidateWarnings = new ValidateWarnings(Seq(ValidateWarning(warnMsg)))
}

case class ValidateWarning(msg: String)