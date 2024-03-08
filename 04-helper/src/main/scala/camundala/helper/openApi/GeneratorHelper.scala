package camundala.helper.openApi

import os.Path

trait GeneratorHelper:
  protected def config: OpenApiConfig

  protected def apiDefinition: ApiDefinition

  protected lazy val superClass: BpmnSuperClass = apiDefinition.superClass
  protected lazy val bpmnPath: Path = config.bpmnPath(superClass.versionPackage)
  protected lazy val bpmnPackage: String = config.bpmnPackage(superClass.versionPackage)

  protected def generateObject(
      name: String,
      params: Option[Seq[ConstrField]] = None,
      intent: String = ""
  ): String =
    val paramObjects = params.view.toSeq.flatten
      .collect:
        case field if field.enumCases.nonEmpty =>
          s"""
             |$intent  enum ${field.tpeName}:
             |$intent    case ${field.enumCases.view.toSeq.flatten.map(fieldName).mkString(", ")}
             |$intent  object ${field.tpeName}:
             |$intent    given ApiSchema[${field.tpeName}] = deriveEnumApiSchema
             |$intent    given InOutCodec[${field.tpeName}] = deriveEnumInOutCodec
             |
             |""".stripMargin
      .mkString

    s"""${intent}object $name:
       |$intent  given ApiSchema[$name] = deriveApiSchema
       |$intent  given InOutCodec[$name] = deriveInOutCodec
       |$paramObjects
       |${intent}end $name
       |""".stripMargin
  end generateObject

  protected def printField(
      field: ConstrField,
      parentName: String,
      intent: String = "  "
  ): String =
    s"""${
        printDescrOpt(field, s"$intent  ").map(d => s"$intent$d\n").getOrElse("")
      }$intent${fieldName(field.name)}: ${
        printFieldType(field, Some(parentName))
      } = ${printFieldValue(field, Some(parentName))},
       |""".stripMargin

  protected def printDescr(elem: OpenApiElem): String =
    printDescrOpt(elem).getOrElse("")

  protected def printDescrOpt(elem: OpenApiElem, intent: String = ""): Option[String] =
    printDescrTextOpt(elem, intent)
      .map: descr =>
        s"@description($descr)"

  end printDescrOpt

  protected def printDescrTextOpt(elem: OpenApiElem, intent: String = ""): Option[String] =
    val format = elem match
    case f: ConstrField if f.format.nonEmpty => s"\n- Format: ${f.format.mkString}"
    case _ => ""
    elem.descr
      .map: descr =>
        val descrWithFormat = descr + format
        if descrWithFormat.contains("\n")
        then
          // | .stripMargin does not work - eliminated by the real stripMargin of the generator
          s"\"\"\"${descrWithFormat.replace("\n", s"\n    $intent")}\"\"\""
        else
          s""""$descrWithFormat""""
        end if

  end printDescrTextOpt

  private def fieldName(key: String) =
    if key.matches("[$_a-zA-Z][a-zA-Z0-9_$]*") && !reservedWords.contains(key)
    then key
    else s"`$key`"

  protected def printFieldType(field: ConstrField, parentName: Option[String] = None): String =
    val enumPrefix = field.enumCases.flatMap(_ => parentName).map(n => s"$n.").mkString
    val tpe = enumPrefix + field.tpeName

    val typeWithWrapper = field.wrapperType
      .map: wt =>
        s"$wt[$tpe]"
      .getOrElse:
        tpe
    if field.isOptional then s"Option[$typeWithWrapper]" else typeWithWrapper
  end printFieldType

  protected def printFieldValue(
      field: ConstrField,
      parentName: Option[String] = None
  ): String =
    lazy val withEnum =
      if parentName.contains(field.tpeName)
      then
        Some("/* Same type as Parent (recursive) - automatic generation not supported */")
      else
        Some(field.tpeName)
          .flatMap:
            case tpeName if field.enumCases.nonEmpty =>
              Some(
                parentName.map(n => s"$n.").getOrElse(
                  ""
                ) + s"${fieldName(tpeName)}.${fieldName(field.defaultEnumCase)}"
              )
            case tpeName => apiDefinition.serviceClasses
                .find:
                  _.name == tpeName
                .map:
                  case e: BpmnEnum =>
                    s"$tpeName.${e.cases.head.name}()"
                  case _: BpmnClass =>
                    s"$tpeName()"

    val exampleValue: Option[String] =
      config.implMapping.get(field.tpeName)
        .map: e =>
          e(field.defaultValueAsStr)
        .orElse(withEnum)

    val value = field.wrapperType
      .map:
        case WrapperType.Seq =>
          exampleValue.map(ex => s"${WrapperType.Seq.impl}($ex)").getOrElse(s"Seq.empty[${field.tpeName}]")
        case WrapperType.Set =>
          exampleValue.map(ex => s"${WrapperType.Set.impl}($ex)").getOrElse(s"Set.empty[${field.tpeName}]")
      .orElse(exampleValue)

    if field.isOptional then s"$value" else value.getOrElse("THIS SHOULD NOT HAPPEN")
  end printFieldValue

  private lazy val reservedWords = Seq(
    "abstract",
    "case",
    "catch",
    "class",
    "clone",
    "def",
    "do",
    "else",
    "extends",
    "false",
    "final",
    "finally",
    "for",
    "forSome",
    "if",
    "implicit",
    "import",
    "lazy",
    "match",
    "new",
    "null",
    "object",
    "override",
    "package",
    "private",
    "protected",
    "return",
    "sealed",
    "super",
    "this",
    "throw",
    "trait",
    "try",
    "true",
    "type",
    "val",
    "var",
    "while",
    "with",
    "yield"
  )

end GeneratorHelper
