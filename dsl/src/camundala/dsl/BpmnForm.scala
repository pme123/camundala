package camundala.dsl

import camundala.dsl.GeneratedForm.FormField


sealed trait BpmnForm

case class EmbeddedForm(formRef: Identifier)
  extends BpmnForm

case class GeneratedForm(fields: Seq[FormField] = Seq.empty)
  extends BpmnForm {
  def fields(fld: FormField, flds: FormField*): GeneratedForm = copy(fields = (fields :+ fld) ++ flds)

  def ---(fld: FormField, flds: FormField*): GeneratedForm = fields(fld, flds: _*)
}

object GeneratedForm {

  import FormFieldType._

  def textField(id: Identifier): SimpleField =
    SimpleField(id)

  def booleanField(id: Identifier): SimpleField =
    SimpleField(id, `type` = BooleanType)

  def longField(id: Identifier): SimpleField =
    SimpleField(id, `type` = LongType)

  def dateField(id: Identifier): SimpleField =
    SimpleField(id, `type` = DateType)

  def enumField(id: Identifier): EnumField =
    EnumField(id)


  sealed trait FormField

  case class SimpleField(id: Identifier,
                         label: String = "",
                         `type`: FormFieldType = StringType,
                         defaultValue: String = "",
                         width: Int = 16,
                         constraints: Seq[Constraint] = Seq.empty,
                         properties: Seq[Property] = Seq.empty)
    extends FormField {

    def fieldType(fieldType: FormFieldType): SimpleField = copy(`type` = fieldType)

    def label(l: String): SimpleField = copy(label = l)

    def default(d: String): SimpleField = copy(defaultValue = d)

    def width(w: Int): SimpleField = copy(width = w)
  }

  case class EnumField(id: Identifier,
                       label: String = "",
                       defaultValue: String = null, // must be null otherwise Camunda fails
                       values: EnumValues = EnumValues.none,
                       width: Int = 16,
                       constraints: Seq[Constraint] = Seq.empty,
                       properties: Seq[Property] = Seq.empty)
    extends FormField {
    val `type`: FormFieldType = EnumType

    def label(l: String): EnumField = copy(label = l)

    def default(d: String): EnumField = copy(defaultValue = d)

    def width(w: Int): EnumField = copy(width = w)

    def value(key: Identifier, value: String): EnumField = copy(values = values :+ EnumValue(key, value))
  }

  case class EnumValues(enums: Seq[EnumValue]) {
    def :+(value: EnumValue): EnumValues = copy(enums :+ value)

  }

  object EnumValues {
    def none: EnumValues = EnumValues(Seq.empty)
  }

  case class EnumValue(key: Identifier, label: String)


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
