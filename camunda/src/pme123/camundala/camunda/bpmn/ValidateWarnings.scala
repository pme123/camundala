package pme123.camundala.camunda.bpmn

import org.camunda.bpm.model.bpmn.{BpmnModelInstance, instance => camunda}

case class MergeResult(camModel: BpmnModelInstance, warnings: ValidateWarnings)
case class MergeProcess(process: camunda.Process, warnings: ValidateWarnings)
case class MergeTask(task: camunda.Task, warnings: ValidateWarnings)

case class ValidateWarnings(errors: Seq[ValidateWarning]) {

  def ++(other: ValidateWarnings): ValidateWarnings =
    ValidateWarnings(errors ++ other.errors)
}

object ValidateWarnings {
  def none: ValidateWarnings = ValidateWarnings(Seq.empty)

  def apply(errorMsg: String): ValidateWarnings = new ValidateWarnings(Seq(ValidateWarning(errorMsg)))
}

case class ValidateWarning(msg: String)