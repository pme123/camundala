package camundala.helper.openApi

case class ServiceClassesGenerator()(using
    val apiDefinition: ApiDefinition,
    val config: OpenApiConfig
) extends GeneratorHelper:

  lazy val generate: Unit =
    os.makeDir.all(bpmnPath / "schema")
    apiDefinition
      .serviceClasses
      .map:
        generateSchema
      .foreach:
        case key -> content =>
          os.write.over(bpmnPath / "schema" / s"$key.scala", content)
  // println(content)
  end generate

  private def generateSchema(
      classOrEnum: IsFieldType
  ): (String, String) =
    classOrEnum.className ->
      s"""package $bpmnPackage.schema
         |
         |${printDescr(classOrEnum)}
         |${
          classOrEnum match
            case e: BpmnEnum =>
              val params = e.cases
                .collect:
                  case c: BpmnClass =>
                    c.fields
                .flatten
              generateEnum(e) + generateObject(classOrEnum.className, Some(params))
            case c: BpmnClass =>
              generateCaseClass(c) + generateObject(classOrEnum.className, Some(c.fields))
        }
         |""".stripMargin

  private def generateCaseClass(
      bpmnClass: BpmnClass,
      enumName: Option[String] = None,
      intent: String = ""
  ) =
    val key = bpmnClass.className
    s"""${intent}case${enumName.map(_ => "").getOrElse(" class")} $key(
       |${
        bpmnClass.fields
          .map:
            printField(_, enumName.getOrElse(bpmnClass.className), s"  $intent")
          .mkString
      }
       |$intent)
       |""".stripMargin
  end generateCaseClass

  private def generateEnum(bpmnEnum: BpmnEnum, intent: String = ""): String =
    val key = bpmnEnum.className
    s"""$intent${printDescr(bpmnEnum)}
       |${intent}enum $key:
       |${
        bpmnEnum.cases
          .map:
            case bpmnEnum: BpmnEnum =>
              generateEnum(bpmnEnum, s"  $intent")
            case bpmnClass: BpmnClass =>
              generateCaseClass(bpmnClass, Some(key), s"  $intent")
            case enumCase: EnumCase =>
              s"""$intent  ${printDescr(enumCase)}
                 |$intent  case ${enumCase.className}""".stripMargin
          .mkString
      }
       |${intent}end $key
       |""".stripMargin
  end generateEnum

end ServiceClassesGenerator
