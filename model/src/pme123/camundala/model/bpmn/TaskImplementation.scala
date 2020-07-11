package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._

sealed trait TaskImplementation {

  def staticFiles: Set[StaticFile] = Set.empty

}

sealed trait WithTaskImpl[T] {
  def set(task: T, taskImpl: TaskImplementation): T
}

object WithTaskImpl {

  def apply[A](implicit task: WithTaskImpl[A]): WithTaskImpl[A] = task

  //needed only if we want to support notation: form(...)
  def set[A: WithTaskImpl](task: A, taskImpl: TaskImplementation): A =
    WithTaskImpl[A].set(task, taskImpl)

  //type class instances
  def instance[A](func: (A, TaskImplementation) => A): WithTaskImpl[A] =
    new WithTaskImpl[A] {
      def set(task: A, taskImpl: TaskImplementation): A =
        func(task, taskImpl)
    }

  implicit val serviceTask: WithTaskImpl[ServiceTask] =
    instance((task, taskImpl) => task.copy(implementation = taskImpl))
  implicit val sendTask: WithTaskImpl[SendTask] =
    instance((task, taskImpl) => task.copy(implementation = taskImpl))

}

object TaskImplementation {

  case class DelegateExpression(expresssion: String)
    extends TaskImplementation

  case class JavaClass(className: String)
    extends TaskImplementation

  case class ExternalTask(topic: String)
    extends TaskImplementation

  /**
    * Only for BusinessRuleTask
    */
  case class DmnImpl(decisionRef: StaticFile,
                     resultVariable: Identifier = "ruleResult",
                     decisionRefBinding: String = "deployment", // only supported
                     mapDecisionResult: String = "singleEntry", // only supported
                    )
    extends TaskImplementation {
    override def staticFiles: Set[StaticFile] = Set(decisionRef)

  }

  object DmnImpl {
    def apply(decisionRef: FilePath): DmnImpl = new DmnImpl(StaticFile(decisionRef))

    def notImplemented: DmnImpl = DmnImpl(StaticFile("NotImplemented"))

  }

}
