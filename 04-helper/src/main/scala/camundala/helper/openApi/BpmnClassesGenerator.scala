package camundala.helper.openApi

case class BpmnClassesGenerator()(using
    val apiDefinition: ApiDefinition,
    val config: OpenApiConfig
) extends GeneratorHelper:

  lazy val generate =
    apiDefinition.bpmnClasses
      .map:
        generateModel
      .foreach:
        case key -> content =>
          os.write.over(config.bpmnPath / s"$key.scala", content)
  end generate

  private def generateModel(serviceObj: BpmnServiceObject) =
    val superClass = apiDefinition.superClass
    val name = serviceObj.name
    val topicName = s"${config.projectTopicName}${superClass.versionTag}"
    val printInOut: Option[ConstrField] => String =
      _.map(printField(_, serviceObj.name, "    ")).mkString("\n", "", "  ")
    val content =
      s"""package ${config.bpmnPackage}
         |
         |import ${config.schemaPackage}.*
         |
         |object $name
         |  extends ${superClass.name}:
         |
         |  final val topicName = "$topicName.${serviceObj.topicName}"
         |
         |  val descr = ${printDescrTextOpt(serviceObj).getOrElse("-")}
         |  val path = "${serviceObj.method}: ${serviceObj.path}"
         |  
         |  type ServiceIn = ${serviceObj.in.map(printFieldType(_)).getOrElse("NoInput")}
         |  type ServiceOut = ${serviceObj.out.map(printFieldType(_)).getOrElse("NoOutput")}
         |  lazy val serviceInExample = ${serviceObj.in.map(printFieldValue(_)).map(_.replace(
          "Some(",
          "Option("
        )).getOrElse(
          "NoInput()"
        )}
         |  lazy val serviceMock = MockedServiceResponse.success${serviceObj.mockStatus}${
          serviceObj.out.map(
            printFieldValue(_)
          ).map(v => s"(${v.replace("Some(", "Option(")})").getOrElse("")
        }
         |
         |  case class In(${
          serviceObj.inputParams
            .map:
              _.map(printField(_, "In", "    "))
                .mkString("\n", "", "")
            .getOrElse("")
        }${printInOut(serviceObj.in)})
         |${generateObject("In", serviceObj.inputParams, "  ")}
         |
         |  case class Out(${printInOut(serviceObj.out)})
         |${generateObject("Out", None, "  ")}
         |  lazy val example =
         |    serviceTaskExample(
         |      in = In(),
         |      out = Out(),
         |      serviceMock,
         |      serviceInExample
         |    )
         |end $name
         |""".stripMargin
    serviceObj.name -> content
  end generateModel
end BpmnClassesGenerator
