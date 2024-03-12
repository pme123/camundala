package camundala.helper.openApi

import sttp.model.Method

case class ApiDefinition(
    superClass: BpmnSuperClass,
    serviceClasses: Seq[IsFieldType] = Seq.empty,
    bpmnClasses: Seq[BpmnServiceObject] = Seq.empty
)
sealed trait OpenApiElem:
  def name: String
  def descr: Option[String]

  lazy val niceName: String = // e.g `Authorization Check Get`
    name.foldLeft(""):
      case result -> ch if ch.isUpper =>
        result + " " + ch
      case result -> ch =>
        result + ch
    .trim
end OpenApiElem

sealed trait BpmnClassOrEnum extends OpenApiElem
sealed trait EnumCase extends OpenApiElem
sealed trait IsFieldType extends OpenApiElem

case class BpmnSuperClass(
    title: String,
    version: Option[String],
    descr: Option[String],
    externalDescr: Option[String],
    externalUrl: Option[String]
):
  lazy val name: String = titleName + versionTag
  lazy val versionTag: String = version.map("V" + _.split('.').head).getOrElse("")
  lazy val versionPackage: String = versionTag.toLowerCase
  
  private lazy val titleName = title
    .replace(' ', '-')
    .split('-')
    .map: n =>
      n.head.toUpper + n.tail
    .filterNot: n =>
      Seq("api", "rest").contains(n.toLowerCase())
    .mkString
end BpmnSuperClass

case class BpmnClass(
    name: String,
    descr: Option[String],
    fields: Seq[ConstrField] = Seq.empty
) extends BpmnClassOrEnum, IsFieldType, EnumCase:
  def withFields(fields: Seq[ConstrField]) =
    copy(fields = fields)
end BpmnClass

case class BpmnEnum(
    name: String,
    descr: Option[String],
    cases: Seq[EnumCase]
) extends BpmnClassOrEnum, IsFieldType, EnumCase

case class BpmnEnumCase(
    name: String,
    descr: Option[String]
) extends EnumCase

case class BpmnServiceObject(
    name: String,
    topicName: String,
    path: String,
    descr: Option[String],
    method: Method,
    respStatus: Option[String],
    in: Option[ConstrField],
    out: Option[ConstrField],
    inputParams: Option[Seq[ConstrField]]
) extends BpmnClassOrEnum, EnumCase:

  lazy val mockStatus: String = respStatus.getOrElse("204")
end BpmnServiceObject

/*
  private lazy val printType = tpe match
  case SimpleRef(value) => value.getClass.getSimpleName
  case SimpleValue(value) => value.getClass.getSimpleName
  case e: BpmnEnum => e.name
  case c: BpmnClass => c.name
  private lazy val printTypeValue = tpe match
  case SimpleValue(value: String) => s"\"$value\""
  case SimpleValue(value) => value
  case SimpleRef(value) => value
  case e: BpmnEnum => s"${e.name}.${e.cases.head.name}()"
  case c: BpmnClass => s"${c.name}()"
 */
case class ConstrField(
    name: String,
    descr: Option[String],
    tpeName: String,
    isOptional: Boolean,
    wrapperType: Option[WrapperType] = None,
    format: Option[String] = None,
    enumCases: Option[Seq[String]],
    defaultValueAsStr: Option[String] = None,
    example: Option[Json] = None
) extends OpenApiElem:
  lazy val defaultEnumCase =
    enumCases
      .map:
        _.head
      .getOrElse("NO EnumCases!")

  def withDefaultValueAsStr(value: Option[String]): ConstrField =
    copy(defaultValueAsStr = value)
end ConstrField

enum WrapperType(val impl: String):
  case Set extends WrapperType("Set")
  case Seq extends WrapperType("Seq")
end WrapperType
