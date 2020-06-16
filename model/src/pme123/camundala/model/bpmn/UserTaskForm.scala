package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.Constraint
import pme123.camundala.model.bpmn.UserTaskForm.FormFieldType.{EnumType, StringType}

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

  }

  sealed trait FormField {
    def id: PropKey

    def label: String

    def `type`: FormFieldType

    def defaultValue: String

    def validations: Seq[Constraint]

    def properties: Seq[Prop]
  }

  object FormField {

    case class SimpleField(id: PropKey,
                           label: String = "",
                           `type`: FormFieldType = StringType,
                           defaultValue: String = "",
                           validations: Seq[Constraint] = Seq.empty,
                           properties: Seq[Prop] = Seq.empty)
      extends FormField {

      def label(l: String): SimpleField = copy(label = l)

      def default(d: String): SimpleField = copy(defaultValue = d)

      def validate(constraint: Constraint): SimpleField = copy(validations = validations :+ constraint)

      def prop(prop: (PropKey, String)): SimpleField = copy(properties = properties :+ Prop(prop._1, prop._2))
    }

    case class EnumField(id: PropKey,
                         label: String = "",
                         defaultValue: String = null, // must be null otherwise Camunda fails
                         values: EnumValues = EnumValues.none,
                         validations: Seq[Constraint] = Seq.empty,
                         properties: Seq[Prop] = Seq.empty)
      extends FormField {
      val `type`: FormFieldType = EnumType

      def label(l: String): EnumField = copy(label = l)

      def default(d: String): EnumField = copy(defaultValue = d)

      def value(prop: (PropKey, String)): EnumField = copy(values = values :+ EnumValue(prop._1, prop._2))

      def validate(constraint: Constraint): EnumField = copy(validations = validations :+ constraint)

      def prop(prop: (PropKey, String)): EnumField = copy(properties = properties :+ Prop(prop._1, prop._2))
    }

    case class EnumValues(enums: Seq[EnumValue]) {
      def :+(value: EnumValue): EnumValues = copy(enums :+ value)

    }

    object EnumValues {
      def none: EnumValues = EnumValues(Seq.empty)
    }

    case class EnumValue(key: PropKey, label: String)

    sealed trait Constraint {
      def name: PropKey

      def config: Option[String]
    }

    object Constraint {

      case class Custom(name: PropKey, config: Option[String]) extends Constraint

      case object Required extends Constraint {
        val name: PropKey = "required"

        val config: Option[String] = None
      }

      case object Readonly extends Constraint {
        val name: PropKey = "readonly"

        val config: Option[String] = None
      }

      sealed trait MinMax extends Constraint {

        def value: Int

        val config: Option[String] = Some(s"$value")
      }

      case class Minlength(value: Int) extends MinMax {
        val name: PropKey = "minlength"
      }

      case class Maxlength(value: Int) extends MinMax {
        val name: PropKey = "maxlength"
      }

      case class Min(value: Int) extends MinMax {
        val name: PropKey = "min"
      }

      case class Max(value: Int) extends MinMax {
        val name: PropKey = "max"
      }

    }
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
