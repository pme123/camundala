package pme123.camundala.model.bpmn

import eu.timepit.refined.auto._
import pme123.camundala.model.bpmn.UserTaskForm.FormField.{Constraint, Property}
import pme123.camundala.model.bpmn.UserTaskForm.FormFieldType.{EnumType, StringType}

sealed trait UserTaskForm {

  def staticFiles: Set[StaticFile] = Set.empty

}

object UserTaskForm {

  case class EmbeddedDeploymentForm(form: StaticFile)
    extends UserTaskForm {
    override def staticFiles: Set[StaticFile] = Set(form)

  }

  case class GeneratedForm(fields: Seq[FormField])
    extends UserTaskForm

  sealed trait FormField {
    def id: PropKey

    def label: String

    def `type`: FormFieldType

    def defaultValue: String

    def validations: Seq[Constraint]

    def properties: Seq[Property]
  }

  object FormField {

    case class SimpleField(id: PropKey,
                           label: String = "",
                           `type`: FormFieldType = StringType,
                           defaultValue: String = "",
                           validations: Seq[Constraint] = Seq.empty,
                           properties: Seq[Property] = Seq.empty)
      extends FormField

    case class EnumField(id: PropKey,
                         label: String = "",
                         defaultValue: String = "",
                         values: EnumValues,
                         validations: Seq[Constraint] = Seq.empty,
                         properties: Seq[Property] = Seq.empty)
      extends FormField {
      val `type`: FormFieldType = EnumType
    }

    case class EnumValues(enums: Seq[EnumValue])

    case class EnumValue(key: PropKey, label: String)

    sealed trait Constraint {
      def name: PropKey

      def config: Option[String]
    }

    object Constraint {

      case class Custom(name: PropKey, config:  Option[String]) extends Constraint

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

      case class Min(value: Int)  extends MinMax {
        val name: PropKey = "min"
      }
      case class Max(value: Int)  extends MinMax {
        val name: PropKey = "max"
      }
    }

    case class Property(id: PropKey, value: String)

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
