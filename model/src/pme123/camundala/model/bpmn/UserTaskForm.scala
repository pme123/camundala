package pme123.camundala.model.bpmn

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
  }

  object FormField {

    case class SimpleField(id: PropKey, label: String = "", `type`: FormFieldType = StringType, defaultValue: String = "")
      extends FormField

    case class EnumField(id: PropKey, label: String = "", defaultValue: String = "", values: EnumValues)
      extends FormField {
      val `type`: FormFieldType = EnumType
    }

    case class EnumValues(enums: Seq[EnumValue])

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
