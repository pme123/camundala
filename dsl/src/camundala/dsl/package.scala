package camundala

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex

package object dsl {
  type IdRegex = MatchesRegex["""^[a-zA-Z_][\w\-\.]+$"""]
  type EmailRegex = MatchesRegex["""(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|"(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21\x23-\x5b\x5d-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])*")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\x01-\x08\x0b\x0c\x0e-\x1f\x21-\x5a\x53-\x7f]|\\[\x01-\x09\x0b\x0c\x0e-\x7f])+)\])"""]

  type Identifier = String Refined IdRegex
  type Email = String Refined EmailRegex


  def bpmn(id: Identifier): Bpmn = Bpmn(id)

  def process(id: Identifier): BpmnProcess = BpmnProcess(id)

  def user(id: Identifier): BpmnUser = BpmnUser(id)

  def group(id: Identifier): BpmnGroup = BpmnGroup(id)

  def startEvent(id: Identifier): StartEvent = StartEvent(id)

  def endEvent(id: Identifier): EndEvent = EndEvent(id)

  def userTask(id: Identifier): UserTask = UserTask(id)

  def serviceTask(id: Identifier): ServiceTask = ServiceTask(id)

  def sendTask(id: Identifier): SendTask = SendTask(id)

  def businessRuleTask(id: Identifier): BusinessRuleTask = BusinessRuleTask(id)

  def callActivity(id: Identifier): CallActivity = CallActivity(id)

  def sequenceFlow(id: Identifier): SequenceFlow = SequenceFlow(id)

  def embeddedForm(id: Identifier): EmbeddedForm = EmbeddedForm(id)

  def generatedForm(): GeneratedForm = GeneratedForm()

  implicit class HasGroupsOps[A: HasGroups](a: A) {
    def :+(elem: BpmnGroup): A =
      HasGroups[A].:+(a, elem)

    def ++(elems: Seq[BpmnGroup]): A =
      HasGroups[A].++(a, elems)
  }

  implicit class HasUsersOps[A: HasUsers](a: A) {
    def :+(elem: BpmnUser): A =
      HasUsers[A].:+(a, elem)

    def ++(elems: Seq[BpmnUser]): A =
      HasUsers[A].++(a, elems)
  }

  implicit class HasFormOps[A: HasForm](a: A) {
    def form(form: BpmnForm): A =
      HasForm[A].form(a, form)

    def embeddedForm(formRef: Identifier): A =
      HasForm[A].form(a, EmbeddedForm(formRef))
  }

  implicit class HasPropOps[A: HasProp](a: A) {
    def prop(p: Property): A =
      HasProp[A].prop(a, p)

    def prop(key: Identifier, value: String): A =
      prop(Property(key, value))

    def prop(key: Identifier, maybeValue: Option[String]): A =
      maybeValue match {
        case None => a
        case Some(value) => prop(key, value)
      }
  }

  implicit class HasInParametersOps[A: HasInParameters](a: A) {

    def inputParam(p: Parameter): A =
      HasInParameters[A].inputParam(a, p)

    def inputText(key: Identifier, value: String): A =
      inputParam(TextParam(key, value))

    def inputGroovy(key: Identifier, script: String): A =
      inputParam(ScriptParam(key, script, Condition.Groovy))

    def inputGroovyRef(key: Identifier, ref: String): A =
      inputParam(ScriptRefParam(key, ref, Condition.Groovy))

  }

  implicit class HasOutParametersOps[A: HasOutParameters](a: A) {

    def outputParam(p: Parameter): A =
      HasOutParameters[A].outputParam(a, p)

    def outputText(key: Identifier, value: String): A =
      outputParam(TextParam(key, value))

    def outputGroovy(key: Identifier, script: String): A =
      outputParam(ScriptParam(key, script, Condition.Groovy))

    def outputGroovyRef(key: Identifier, ref: String): A =
      outputParam(ScriptRefParam(key, ref, Condition.Groovy))
  }

  implicit class HasConditionOps[A: HasCondition](a: A) {

    def condition(c: Condition): A =
      HasCondition[A].condition(a, c)

    def expression(value: String): A =
      condition(ExpressionCond(value))

    def groovy(script: String): A =
      condition(ScriptCond(script, Condition.Groovy))

    def groovyRef(ref: String): A =
      condition(ScriptRefCond(ref, Condition.Groovy))
  }

  implicit class HasTaskImplOps[A: HasTaskImpl](a: A) {

    def implementation(ti: TaskImplementation): A =
      HasTaskImpl[A].implementation(a, ti)

    def delegate(expression: String): A =
      implementation(DelegateExpression(expression))

    def expression(expression: String): A =
      implementation(Expression(expression))

    def javaClass(className: String): A =
      implementation(JavaClass(className))

    def external(topic: String): A =
      implementation(ExternalTask(topic))
  }

  implicit class HasDmnImplOps[A: HasDmnImpl](a: A) {

    def dmn(ti: DmnImpl): A =
      HasDmnImpl[A].dmn(a, ti)

    def dmn(decisionRef: Identifier): A =
      dmn(DmnImpl(decisionRef))
  }

  implicit class HasConstraintOps[A: HasConstraint](a: A) {

    import Constraint._

    def constraint(constraint: Constraint): A =
      HasConstraint[A].constraint(a, constraint)

    def readonly: A = constraint(Readonly)

    def required: A = constraint(Required)

    def minlength(value: Int): A = constraint(Minlength(value))

    def maxlength(value: Int): A = constraint(Maxlength(value))

    def min(value: Int): A = constraint(Min(value))

    def max(value: Int): A = constraint(Max(value))

    def custom(name: Identifier, config: Option[String]): A = constraint(Custom(name, config))
  }

}
