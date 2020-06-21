package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormField._
import pme123.camundala.model.bpmn.UserTaskForm.GeneratedForm.FormFieldType._

import scala.annotation.nowarn

trait HasForm
  extends BpmnNode {

  def maybeForm: Option[UserTaskForm]

  def formStaticFiles: Set[StaticFile] = maybeForm.toSet[UserTaskForm].flatMap(_.staticFiles)
}

sealed trait WithForm[T] {
  def form(node: T, form: UserTaskForm): T
}

object WithForm {

  def apply[A](implicit node: WithForm[A]): WithForm[A] = node

  //needed only if we want to support notation: form(...)
  def form[A: WithForm](node: A, form: UserTaskForm): A =
    WithForm[A].form(node, form)

  //type class instances
  def instance[A](func: (A, UserTaskForm) => A): WithForm[A] =
    new WithForm[A] {
      def form(field: A, form: UserTaskForm): A =
        func(field, form)
    }

  implicit val userTask: WithForm[UserTask] =
    instance((node, form) => node.copy(maybeForm = Some(form)))

  implicit val enumField: WithForm[StartEvent] =
    instance((node, form) => node.copy(maybeForm = Some(form)))

}

sealed trait UserTaskForm {

  def staticFiles: Set[StaticFile] = Set.empty

}

object UserTaskForm {

  case class EmbeddedDeploymentForm(form: StaticFile)
    extends UserTaskForm {
    override def staticFiles: Set[StaticFile] = Set(form)

  }

  case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
    extends UserTaskForm {
    def field(fld: FormField): GeneratedForm = copy(fields = fields :+ fld)

    def ---(fld: FormField): GeneratedForm = field(fld)

    def allFields(): Seq[FormField] = fields.flatMap(_.allFields())
  }

  object GeneratedForm {
    def textField(id: String): SimpleField =
      SimpleField(id)

    def booleanField(id: String): SimpleField =
      SimpleField(id, `type` = BooleanType)

    def longField(id: String): SimpleField =
      SimpleField(id, `type` = LongType)

    def dateField(id: String): SimpleField =
      SimpleField(id, `type` = DateType)

    def enumField(id: String): EnumField =
      EnumField(id)

    def groupField(id: String): GroupField =
      GroupField(id)

    def rowGroupField(id: String): RowGroupField =
      RowGroupField(id)

    sealed trait FormField {

      def id: String

      def label: String

      def `type`: FormFieldType

      def defaultValue: String

      def width: Int

      def validations: Seq[Constraint]

      def properties: Seq[Prop]

      def allProperties: Seq[Prop] =
        properties :+ Prop("width", s"$width")

      def allFields(): Seq[FormField] = Seq(this)

      @nowarn("cat=unused-params")
      def prop(key: PropKey, value: String): FormField = this

    }

    object FormField {

      case class GroupField(id: String,
                            label: String = "",
                            width: Int = 16,
                            fields: Seq[FormField] = Seq.empty)
        extends FormField {

        val `type`: FormFieldType = StringType
        val defaultValue: String = ""
        val validations: Seq[Constraint] = Seq.empty
        val properties: Seq[Prop] = Seq.empty

        def label(l: String): GroupField = copy(label = l)

        def width(w: Int): GroupField = copy(width = w)

        def field(fld: FormField): GroupField = copy(fields = fields :+ fld)

        def ---(fld: FormField): GroupField = field(fld)

        override def allProperties: Seq[Prop] =
          super.allProperties :+ Prop("display", "group")

        override def allFields(): Seq[FormField] = this +: fields.flatMap(_.allFields()).map(withGroup)

        private def withGroup(field: FormField): FormField =
          field.prop("group", id)
      }

      case class RowGroupField(id: String,
                               fields: Seq[FormField] = Seq.empty)
        extends FormField {

        val label: String = ""
        val `type`: FormFieldType = StringType
        val defaultValue: String = ""
        val width: Int = 16
        val validations: Seq[Constraint] = Seq.empty
        val properties: Seq[Prop] = Seq.empty

        def field(fld: SimpleField): RowGroupField = copy(fields = fields :+ fld)

        def ---(fld: SimpleField): RowGroupField = field(fld)

        def field(fld: EnumField): RowGroupField = copy(fields = fields :+ fld)

        def ---(fld: EnumField): RowGroupField = field(fld)

        override def allFields(): Seq[FormField] = fields.flatMap(_.allFields()).map(withGroup)

        private def withGroup(field: FormField): FormField =
          field.prop("fieldGroup", id)

      }

      case class SimpleField(id: String,
                             label: String = "",
                             `type`: FormFieldType = StringType,
                             defaultValue: String = "",
                             width: Int = 16,
                             validations: Seq[Constraint] = Seq.empty,
                             properties: Seq[Prop] = Seq.empty)
        extends FormField {

        def fieldType(fieldType: FormFieldType): SimpleField = copy(`type` = fieldType)

        def label(l: String): SimpleField = copy(label = l)

        def default(d: String): SimpleField = copy(defaultValue = d)

        def width(w: Int): SimpleField = copy(width = w)

        override def prop(key: PropKey, value: String): SimpleField = copy(properties = properties :+ Prop(key, value))
      }

      case class EnumField(id: String,
                           label: String = "",
                           defaultValue: String = null, // must be null otherwise Camunda fails
                           values: EnumValues = EnumValues.none,
                           width: Int = 16,
                           validations: Seq[Constraint] = Seq.empty,
                           properties: Seq[Prop] = Seq.empty)
        extends FormField {
        val `type`: FormFieldType = EnumType

        def label(l: String): EnumField = copy(label = l)

        def default(d: String): EnumField = copy(defaultValue = d)

        def width(w: Int): EnumField = copy(width = w)

        def value(key: PropKey, value: String): EnumField = copy(values = values :+ EnumValue(key, value))

        override def prop(key: PropKey, value: String): EnumField = copy(properties = properties :+ Prop(key, value))
      }

      case class EnumValues(enums: Seq[EnumValue]) {
        def :+(value: EnumValue): EnumValues = copy(enums :+ value)

      }

      object EnumValues {
        def none: EnumValues = EnumValues(Seq.empty)
      }

      case class EnumValue(key: PropKey, label: String)

    }

    sealed trait FormFieldType {
      def name: String
    }

    object FormFieldType {

      case object StringType extends FormFieldType {
        val name = "string"
      }

      case object BooleanType extends FormFieldType {
        val name = "boolean"
      }

      case object EnumType extends FormFieldType {
        val name = "enum"
      }

      case object LongType extends FormFieldType {
        val name = "long"
      }

      case object DateType extends FormFieldType {
        val name = "date"
      }

    }

  }

  sealed trait WithConstraint[T] {
    def validate(field: T, constraint: Constraint): T
  }

  object WithConstraint {

    def apply[A](implicit validation: WithConstraint[A]): WithConstraint[A] = validation

    //needed only if we want to support notation: show(...)
    def validate[A: WithConstraint](validation: A, constraint: Constraint): A =
      WithConstraint[A].validate(validation, constraint)

    //type class instances
    def instance[A](func: (A, Constraint) => A): WithConstraint[A] =
      new WithConstraint[A] {
        def validate(field: A, constraint: Constraint): A =
          func(field, constraint)
      }

    implicit val simpleField: WithConstraint[SimpleField] =
      instance((field, constraint) => field.copy(validations = field.validations :+ constraint))

    implicit val enumField: WithConstraint[EnumField] =
      instance((field, constraint) => field.copy(validations = field.validations :+ constraint))

  }

}
