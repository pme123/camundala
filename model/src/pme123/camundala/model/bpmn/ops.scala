package pme123.camundala.model.bpmn

import pme123.camundala.model.bpmn.Constraint.{Custom, Max, Maxlength, Min, Minlength, Readonly, Required}
import pme123.camundala.model.bpmn.ScriptLanguage.Groovy
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm

object ops {

  implicit class FormularOps[A: WithForm](a: A) {
    def form(form: UserTaskForm): A =
      WithForm[A].form(a, form)

    def ===(utf: UserTaskForm): A = form(utf)

    def embeddedForm(fileName: FilePath, resourcePath: PathElem): A =
      form(EmbeddedDeploymentForm(StaticFile(fileName, resourcePath)))
  }

  implicit class WithExtPropertiesOps[A: WithProperties](a: A) {
    def prop(key: PropKey, value: String): A =
      WithProperties[A].prop(a, key, value)
  }

  implicit class WithInOutputsOps[A: WithInOutputs](a: A) {

    def inOutputs(inOutputs: ExtInOutputs): A =
      WithInOutputs[A].inOutputs(a, inOutputs)

    def extInOutputs(): ExtInOutputs =
      WithInOutputs[A].extInOutputs(a)

    def inputExpression(key: PropKey, expression: String): A =
      inOutputs(extInOutputs().inputExpression(key, expression))

    def inputExternal(key: PropKey, scriptPath: FilePath, language: ScriptLanguage = Groovy, includes: Seq[String] = Seq.empty): A =
      inOutputs(extInOutputs().inputExternal(key, scriptPath, language, includes))

    def inputInline(key: PropKey, inlineScript: String): A =
      inOutputs(extInOutputs().inputInline(key, inlineScript))

    def inputJson(key: PropKey, json: String): A =
      inOutputs(extInOutputs().inputJson(key, json))

    def outputExpression(key: PropKey, expression: String): A =
      inOutputs(extInOutputs().outputExpression(key, expression))
    def outputExternal(key: PropKey, scriptPath: FilePath, language: ScriptLanguage = Groovy, includes: Seq[String] = Seq.empty): A =
      inOutputs(extInOutputs().outputExternal(key, scriptPath, language, includes))
    def outputInline(key: PropKey, inlineScript: String): A =
      inOutputs(extInOutputs().outputInline(key, inlineScript))

    def outputJson(key: PropKey, json: String): A =
      inOutputs(extInOutputs().outputJson(key, json))

  }

  implicit class ValidationOps[A: WithConstraint](a: A) {
    def validate(constraint: Constraint): A =
      WithConstraint[A].validate(a, constraint)

    def readonly: A = validate(Readonly)

    def required: A = validate(Required)

    def minlength(value: Int): A = validate(Minlength(value))

    def maxlength(value: Int): A = validate(Maxlength(value))

    def min(value: Int): A = validate(Min(value))

    def max(value: Int): A = validate(Max(value))

    def custom(name: PropKey, config: Option[String]): A = validate(Custom(name, config))
  }

}