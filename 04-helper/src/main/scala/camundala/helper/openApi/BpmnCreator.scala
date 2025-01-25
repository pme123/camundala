package camundala.helper.openApi

import io.swagger.v3.oas.models.OpenAPI

import scala.jdk.CollectionConverters.*

case class BpmnCreator()(using config: OpenApiConfig):

  def create(openAPI: OpenAPI): ApiDefinition =
    val superClass: BpmnSuperClass =
      BpmnSuperClassCreator(openAPI.getInfo, Option(openAPI.getExternalDocs)).create
    val bpmnClasses                =
      BpmnClassesCreator(openAPI.getPaths.asScala.toMap).create
    val serviceClasses             =
      ServiceClassesCreator(openAPI.getComponents.getSchemas.asScala.toMap).create
    val examples                   = examplesFrom(bpmnClasses)
    val examplesAll                = extractExamples(serviceClasses, examples)
    val updatedServiceClasses      = setDefaultValues(serviceClasses, examplesAll)
    ApiDefinition(
      superClass,
      updatedServiceClasses,
      bpmnClasses
    )
  end create

  private def examplesFrom(bpmnClasses: Seq[BpmnServiceObject]): Map[String, Json] =
    bpmnClasses
      .flatMap: c =>
        def ex(field: Option[ConstrField]) = field.flatMap: in =>
          in.example.map(in.tpeName -> _)

        Seq(ex(c.in), ex(c.out)).flatten
      .toMap

  private def extractExamples(
      serviceClasses: Seq[IsFieldType],
      examples: Map[String, Json]
  ): Map[String, Json] =
    val serviceClassMap: Map[String, IsFieldType] = serviceClasses.map(c => c.className -> c).toMap
    serviceClasses.foldLeft(Map.empty[String, Json]):
      case result -> (serviceClass: BpmnClass) if examples.contains(serviceClass.className) =>
        println(s"Example for: ${serviceClass.className}")
        result ++ extractExample(serviceClass, serviceClassMap, examples(serviceClass.className))
      case result -> (serviceClass: BpmnEnum)                                               => // not supported yet
        println(s"Example not supported for: ${serviceClass.className}")
        result
      case result -> other                                                                  =>
        println(s"No Example for: ${other.className}")
        result
  end extractExamples

  private def extractExample(
      serviceClass: BpmnClass,
      serviceClassMap: Map[String, IsFieldType],
      example: Json
  ): Map[String, Json] =
    val cursor  = example.deepDropNullValues.hcursor
    val cursor2 = if example.isArray then cursor.downArray.downN(0) else cursor
    val exMap   = serviceClass.fields
      .map: field =>
        cursor2.downField(field.name).focus
          .flatMap:
            case j if j.isArray =>
              j.hcursor.downArray.downN(0).focus
            case j              => Some(j)
          .filter:
            _.isObject
          .flatMap: j =>
            serviceClassMap.get(field.tpeName)
              .map:
                case c: BpmnClass =>
                  println(s"Sub Example for: ${c.name}")
                  extractExample(c, serviceClassMap, j)
                case e: BpmnEnum  => // not supported yet
                  println(s"Sub Example not supported for: ${e.name}")
                  Map(field.tpeName -> j)
      .collect:
        case Some(m) => m
      .foldLeft(Map.empty[String, Json]): (result, ex) =>
        result ++ ex

    exMap + (serviceClass.name -> example)
  end extractExample

  private def setDefaultValues(
      serviceClasses: Seq[IsFieldType],
      examples: Map[String, Json]
  ): Seq[IsFieldType] =
    serviceClasses.foldLeft(Seq.empty[IsFieldType]):
      case result -> (serviceClass: BpmnClass) =>
        val fields: Option[Seq[ConstrField]] = examples.get(serviceClass.className)
          .map: json =>
            val cursor = json.deepDropNullValues.hcursor
            serviceClass.fields
              .map: field =>
                val defaultValue = cursor.downField(field.name).focus
                  .map:
                    case j if j.isObject =>
                      field.tpeName + "()"
                    case j if j.isArray  =>
                      j.asArray
                        .map:
                          _.map:
                            case j2 if j2.isObject =>
                              field.tpeName + "()"
                            case j2 if j2.isArray  =>
                              s"Seq.empty[${field.tpeName}] // Seq(Seq()) should not happen"
                            case elem              =>
                              elem.toString
                          .mkString(", ")
                        .getOrElse(s"Seq.empty[${field.tpeName}]")
                    case value           =>
                      value.toString
                field.withDefaultValueAsStr(defaultValue)
        // println(s"EX: ${ex}")

        result :+ fields.map(f => serviceClass.withFields(f)).getOrElse(serviceClass)
      case result -> serviceClass              =>
        result :+ serviceClass
  end setDefaultValues

  private def extractExample(key: String, example: Json): Seq[(String, Json)] =
    val cursor                           = example.deepDropNullValues
      .hcursor
    val subExamples: Seq[(String, Json)] =
      cursor.keys.view.toSeq.flatten
        .map: k =>
          cursor.downField(k)
        .map: f =>
          f.key
            .flatMap: k =>
              f.focus.map(k -> _)
        .collect:
          case Some(k -> v) if v.isObject =>
            extractExample(k, v)
        .flatten
    subExamples :+ key -> example
  end extractExample
end BpmnCreator
