package camundala.dsl

import camundala.dsl.GeneratedForm.{EnumField, SimpleField}


// Type Class for Classes that have Users
sealed trait HasUsers[C] {
  def :+(hasUsers: C, elem: BpmnUser): C

  def ++(hasUsers: C, elems: Seq[BpmnUser]): C
}

object HasUsers {

  def apply[C](implicit has: HasUsers[C]): HasUsers[C] = has

  implicit val candidateUsersHasUsers: HasUsers[CandidateUsers] =
    new HasUsers[CandidateUsers] {
      def :+(hasUsers: CandidateUsers, elem: BpmnUser): CandidateUsers =
        hasUsers.copy(users = hasUsers.users :+ elem)

      def ++(hasUsers: CandidateUsers, elems: Seq[BpmnUser]): CandidateUsers =
        hasUsers.copy(users = hasUsers.users ++ elems)
    }
}

// Type Class for Classes that have BpmnGroups
sealed trait HasGroups[C] {
  def :+(hasGroups: C, elem: BpmnGroup): C

  def ++(hasGroups: C, elems: Seq[BpmnGroup]): C
}

object HasGroups {

  def apply[C](implicit has: HasGroups[C]): HasGroups[C] = has

  implicit val candidateGroupsHasGroups: HasGroups[CandidateGroups] =
    new HasGroups[CandidateGroups] {
      def :+(hasGroups: CandidateGroups, elem: BpmnGroup): CandidateGroups =
        hasGroups.copy(groups = hasGroups.groups :+ elem)

      def ++(hasGroups: CandidateGroups, elems: Seq[BpmnGroup]): CandidateGroups =
        hasGroups.copy(groups = hasGroups.groups ++ elems)
    }
}


// Type Class for Classes that have BpmnForm
sealed trait HasForm[T] {
  def form(hasForm: T, form: BpmnForm): T
}

object HasForm {

  def apply[T](implicit has: HasForm[T]): HasForm[T] = has

  implicit val userTaskHasForm: HasForm[UserTask] =
    new HasForm[UserTask] {
      def form(hasForm: UserTask, bpmnForm: BpmnForm): UserTask = hasForm.copy(maybeForm = Some(bpmnForm))
    }

  implicit val startEventHasForm: HasForm[StartEvent] =
    new HasForm[StartEvent] {
      def form(hasForm: StartEvent, bpmnForm: BpmnForm): StartEvent = hasForm.copy(maybeForm = Some(bpmnForm))
    }
}

sealed trait HasProp[T] {
  def prop(hasProp: T, p: Property): T
}

object HasProp {

  def apply[T](implicit has: HasProp[T]): HasProp[T] = has

  implicit val simpleFieldHasProp: HasProp[SimpleField] =
    new HasProp[SimpleField] {
      def prop(has: SimpleField, p: Property): SimpleField = has.copy(properties = has.properties :+ p)
    }

  implicit val enumFieldHasProp: HasProp[EnumField] =
    new HasProp[EnumField] {
      def prop(has: EnumField, p: Property): EnumField = has.copy(properties = has.properties :+ p)
    }

  implicit val propertiesHasProp: HasProp[Properties] =
    new HasProp[Properties] {
      def prop(has: Properties, p: Property): Properties = has.copy(properties = has.properties :+ p)
    }

  implicit val userTaskHasProp: HasProp[UserTask] =
    new HasProp[UserTask] {
      def prop(has: UserTask, p: Property): UserTask = has.copy(properties = has.properties.prop(p))
    }

  implicit val serviceTaskHasProp: HasProp[ServiceTask] =
    new HasProp[ServiceTask] {
      def prop(has: ServiceTask, p: Property): ServiceTask = has.copy(properties = has.properties.prop(p))
    }

  implicit val sendTaskHasProp: HasProp[SendTask] =
    new HasProp[SendTask] {
      def prop(has: SendTask, p: Property): SendTask = has.copy(properties = has.properties.prop(p))
    }

  implicit val businessRuleTaskHasProp: HasProp[BusinessRuleTask] =
    new HasProp[BusinessRuleTask] {
      def prop(has: BusinessRuleTask, p: Property): BusinessRuleTask = has.copy(properties = has.properties.prop(p))
    }

  implicit val startEventHasProp: HasProp[StartEvent] =
    new HasProp[StartEvent] {
      def prop(has: StartEvent, p: Property): StartEvent = has.copy(properties = has.properties.prop(p))
    }

  implicit val endEventHasProp: HasProp[EndEvent] =
    new HasProp[EndEvent] {
      def prop(has: EndEvent, p: Property): EndEvent = has.copy(properties = has.properties.prop(p))
    }

  implicit val callActivityHasProp: HasProp[CallActivity] =
    new HasProp[CallActivity] {
      def prop(has: CallActivity, p: Property): CallActivity = has.copy(properties = has.properties.prop(p))
    }

  implicit val sequenceFlowHasProp: HasProp[SequenceFlow] =
    new HasProp[SequenceFlow] {
      def prop(has: SequenceFlow, p: Property): SequenceFlow = has.copy(properties = has.properties.prop(p))
    }
}

sealed trait HasInParameters[T] {
  def inputParam(hasParameter: T, p: Parameter): T
}

object HasInParameters {

  def apply[T](implicit has: HasInParameters[T]): HasInParameters[T] = has

  implicit val userTaskHasParameter: HasInParameters[UserTask] =
    new HasInParameters[UserTask] {
      def inputParam(has: UserTask, p: Parameter): UserTask = has.copy(inputParams = has.inputParams :+ p)
    }

  implicit val serviceTaskHasParameter: HasInParameters[ServiceTask] =
    new HasInParameters[ServiceTask] {
      def inputParam(has: ServiceTask, p: Parameter): ServiceTask = has.copy(inputParams = has.inputParams :+ p)
    }

  implicit val sendTaskHasParameter: HasInParameters[SendTask] =
    new HasInParameters[SendTask] {
      def inputParam(has: SendTask, p: Parameter): SendTask = has.copy(inputParams = has.inputParams :+ p)
    }

  implicit val businessRuleTaskHasParameter: HasInParameters[BusinessRuleTask] =
    new HasInParameters[BusinessRuleTask] {
      def inputParam(has: BusinessRuleTask, p: Parameter): BusinessRuleTask = has.copy(inputParams = has.inputParams :+ p)
    }

  implicit val callActivityHasParameter: HasInParameters[CallActivity] =
    new HasInParameters[CallActivity] {
      def inputParam(has: CallActivity, p: Parameter): CallActivity = has.copy(inputParams = has.inputParams :+ p)
    }

  implicit val endEventHasParameter: HasInParameters[EndEvent] =
    new HasInParameters[EndEvent] {
      def inputParam(has: EndEvent, p: Parameter): EndEvent = has.copy(inputParams = has.inputParams :+ p)
    }
}

sealed trait HasOutParameters[T] {
  def outputParam(hasParameter: T, p: Parameter): T
}

object HasOutParameters {

  def apply[T](implicit has: HasOutParameters[T]): HasOutParameters[T] = has

  implicit val userTaskHasParameter: HasOutParameters[UserTask] =
    new HasOutParameters[UserTask] {
      def outputParam(has: UserTask, p: Parameter): UserTask = has.copy(outputParams = has.inputParams :+ p)
    }

  implicit val serviceTaskHasParameter: HasOutParameters[ServiceTask] =
    new HasOutParameters[ServiceTask] {
      def outputParam(has: ServiceTask, p: Parameter): ServiceTask = has.copy(outputParams = has.inputParams :+ p)
    }

  implicit val sendTaskHasParameter: HasOutParameters[SendTask] =
    new HasOutParameters[SendTask] {
      def outputParam(has: SendTask, p: Parameter): SendTask = has.copy(outputParams = has.inputParams :+ p)
    }

  implicit val businessRuleTaskHasParameter: HasOutParameters[BusinessRuleTask] =
    new HasOutParameters[BusinessRuleTask] {
      def inputParam(has: BusinessRuleTask, p: Parameter): BusinessRuleTask = has.copy(inputParams = has.inputParams :+ p)

      def outputParam(has: BusinessRuleTask, p: Parameter): BusinessRuleTask = has.copy(outputParams = has.inputParams :+ p)
    }

  implicit val callActivityHasParameter: HasOutParameters[CallActivity] =
    new HasOutParameters[CallActivity] {
      def outputParam(has: CallActivity, p: Parameter): CallActivity = has.copy(outputParams = has.inputParams :+ p)
    }
}

sealed trait HasCondition[T] {
  def condition(has: T, c: Condition): T
}

object HasCondition {

  def apply[T](implicit has: HasCondition[T]): HasCondition[T] = has

  implicit val sequenceFlowHasParameter: HasCondition[SequenceFlow] =
    new HasCondition[SequenceFlow] {
      def condition(has: SequenceFlow, c: Condition): SequenceFlow = has.copy(maybeExpression = Some(c))
    }
}

sealed trait HasConstraint[T] {
  def constraint(hasConstraint: T, p: Constraint): T
}

object HasConstraint {

  def apply[T](implicit has: HasConstraint[T]): HasConstraint[T] = has

  implicit val simpleFieldHasConstraint: HasConstraint[SimpleField] =
    new HasConstraint[SimpleField] {
      def constraint(has: SimpleField, p: Constraint): SimpleField = has.copy(constraints = has.constraints :+ p)
    }

  implicit val enumFieldHasConstraint: HasConstraint[EnumField] =
    new HasConstraint[EnumField] {
      def constraint(has: EnumField, p: Constraint): EnumField = has.copy(constraints = has.constraints :+ p)
    }

}

sealed trait HasTaskImpl[T] {
  def implementation(task: T, taskImpl: TaskImplementation): T
}

object HasTaskImpl {

  def apply[A](implicit task: HasTaskImpl[A]): HasTaskImpl[A] = task

  implicit val serviceTask: HasTaskImpl[ServiceTask] =
    new HasTaskImpl[ServiceTask] {
      def implementation(task: ServiceTask, taskImpl: TaskImplementation): ServiceTask = task.copy(implementation = taskImpl)
    }

  implicit val sendTask: HasTaskImpl[SendTask] =
    new HasTaskImpl[SendTask] {
      def implementation(task: SendTask, taskImpl: TaskImplementation): SendTask = task.copy(implementation = taskImpl)
    }

  implicit val businessRuleTask: HasTaskImpl[BusinessRuleTask] =
    new HasTaskImpl[BusinessRuleTask] {
      def implementation(task: BusinessRuleTask, taskImpl: TaskImplementation): BusinessRuleTask = task.copy(implementation = taskImpl)
    }

}

sealed trait HasDmnImpl[T] {
  def dmn(task: T, taskImpl: TaskImplementation): T
}

object HasDmnImpl {

  def apply[A](implicit task: HasDmnImpl[A]): HasDmnImpl[A] = task

  implicit val businessRuleTask: HasDmnImpl[BusinessRuleTask] =
    new HasDmnImpl[BusinessRuleTask] {
      def dmn(task: BusinessRuleTask, taskImpl: TaskImplementation): BusinessRuleTask = task.copy(implementation = taskImpl)
    }

}


