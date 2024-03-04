package camundala.helper.openApi

case class BpmnSuperClassGenerator()(using
    val apiDefinition: ApiDefinition,
    val config: OpenApiConfig
) extends GeneratorHelper:
  val superClass = apiDefinition.superClass
  val allSchemas = Map.empty[String, Schema[?]]
  val name = superClass.name

  def generate =
    os.write.over(config.bpmnPath / s"$name.scala", content)
    // println(content)
    name
  end generate

  private lazy val content =
    s"""package ${config.bpmnPackage}
       |
       |object $name:
       |
       |  final val serviceLabel = "${superClass.title} ${superClass.versionTag}"
       |  val description = ${optionalText(superClass.descr)}
       |  val externalDoc = ${optionalText(superClass.externalDescr)}
       |  val externalUrl = ${optionalText(superClass.externalUrl)}
       |
       |trait $name
       |  extends ${config.superBpmnClass}:
       |  final val serviceLabel = $name.serviceLabel
       |  val serviceVersion = "${superClass.version.getOrElse("-")}"
       |end $name
       |""".stripMargin

  private def optionalText(text: Option[String]): String =
    text.map(d => s"""Some("$d")""").getOrElse("None")
end BpmnSuperClassGenerator
