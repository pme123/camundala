package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Constraint.{Custom, Max, Maxlength, Min, Minlength, Readonly, Required}
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm

object ops {

  implicit class FormularOps[A: Formular](a: A) {
    def form(form: UserTaskForm): A =
      Formular[A].form(a, form)

    def ===(utf: UserTaskForm): A = form(utf)

    def embeddedForm(fileName: FilePath, resourcePath: PathElem): A =
      form(EmbeddedDeploymentForm(StaticFile(fileName, resourcePath)))
  }

  implicit class ValidationOps[A: Validation](a: A) {
    def validate(constraint: Constraint): A =
      Validation[A].validate(a, constraint)

    def readonly: A = validate(Readonly)

    def required: A = validate(Required)

    def minlength(value: Int): A = validate(Minlength(value))

    def maxlength(value: Int): A = validate(Maxlength(value))

    def min(value: Int): A = validate(Min(value))

    def max(value: Int): A = validate(Max(value))

    def custom(name: PropKey, config: Option[String]): A = validate(Custom(name, config))
  }

}
