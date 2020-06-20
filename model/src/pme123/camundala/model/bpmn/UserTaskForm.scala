package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.Constraint._
import pme123.camundala.model.bpmn.UserTaskForm.EmbeddedDeploymentForm
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{EnumField, SimpleField}
import pme123.camundala.model.bpmn.UserTaskForm.FormFieldType.{BooleanType, EnumType, StringType}

trait HasForm
  extends BpmnNode {

  def maybeForm: Option[UserTaskForm]

  def formStaticFiles: Set[StaticFile] = maybeForm.toSet[UserTaskForm].flatMap(_.staticFiles)
}

sealed trait Formular[T] {
  def form(node: T, form: UserTaskForm): T
}

object Formular {

  def apply[A](implicit node: Formular[A]): Formular[A] = node

  //needed only if we want to support notation: form(...)
  def form[A: Formular](node: A, form: UserTaskForm): A =
    Formular[A].form(node, form)

  //type class instances
  def instance[A](func: (A, UserTaskForm) => A): Formular[A] =
    new Formular[A] {
      def form(field: A, form:UserTaskForm): A =
        func(field, form)
    }

  implicit val userTask: Formular[UserTask] =
    instance((node, form) => node.copy(maybeForm = Some(form)))

  implicit val enumField: Formular[StartEvent] =
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

    def prop(prop: (PropKey, String)): FormField = this

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

    case class RowFieldGroup(id: String,
                             fields: Seq[FormField] = Seq.empty)
      extends FormField {

      val label: String = ""
      val `type`: FormFieldType = StringType
      val defaultValue: String = ""
      val width: Int = 16
      val validations: Seq[Constraint] = Seq.empty
      val properties: Seq[Prop] = Seq.empty

      def field(fld: SimpleField): RowFieldGroup = copy(fields = fields :+ fld)

      def ---(fld: SimpleField): RowFieldGroup = field(fld)

      def field(fld: EnumField): RowFieldGroup = copy(fields = fields :+ fld)

      def ---(fld: EnumField): RowFieldGroup = field(fld)

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

      override def prop(prop: (PropKey, String)): SimpleField = copy(properties = properties :+ Prop(prop._1, prop._2))
    }

    def text(id: String): SimpleField =
      SimpleField(id)

    def boolean(id: String): SimpleField =
      SimpleField(id, `type` = BooleanType)

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

      def value(prop: (PropKey, String)): EnumField = copy(values = values :+ EnumValue(prop._1, prop._2))

      override def prop(prop: (PropKey, String)): EnumField = copy(properties = properties :+ Prop(prop._1, prop._2))
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

sealed trait Validation[T] {
  def validate(field: T, constraint: Constraint): T
}

object Validation {

  def apply[A](implicit validation: Validation[A]): Validation[A] = validation

  //needed only if we want to support notation: show(...)
  def validate[A: Validation](validation: A, constraint: Constraint): A =
    Validation[A].validate(validation, constraint)

  //type class instances
  def instance[A](func: (A, Constraint) => A): Validation[A] =
    new Validation[A] {
      def validate(field: A, constraint: Constraint): A =
        func(field, constraint)
    }

  implicit val simpleField: Validation[SimpleField] =
    instance((field, constraint) => field.copy(validations = field.validations :+ constraint))

  implicit val enumField: Validation[EnumField] =
    instance((field, constraint) => field.copy(validations = field.validations :+ constraint))

}
