package camundala.helper.openApi

import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.{Operation, PathItem}
import sttp.model.Method

import scala.jdk.CollectionConverters.*

case class BpmnClassesCreator(
    pathMap: Map[String, PathItem]
)(using
    val config: OpenApiConfig
) extends CreatorHelper:

  lazy val create: Seq[BpmnServiceObject] =
    pathMap
      .map:
        case key -> path =>
          extractOperation(key, path)
      .collect:
        case o if o.nonEmpty =>
          o.get
      .map:
        case key -> method -> operation =>
          createModel(key, method, operation)
      .toSeq
  end create

  private def extractOperation(key: String, path: PathItem) =
    val operation = (
      path.getGet,
      path.getPost,
      path.getPut,
      path.getPatch,
      path.getDelete
    ) match
    case (op: Operation, _, _, _, _) =>
      Some(Method.GET -> op)
    case (null, op: Operation, _, _, _) =>
      Some(Method.POST -> op)
    case (null, null, op: Operation, _, _) =>
      Some(Method.PUT -> op)
    case (null, null, null, op: Operation, _) =>
      Some(Method.PATCH -> op)
    case (null, null, null, null, op: Operation) =>
      Some(Method.DELETE -> op)
    case (null, null, null, null, null) =>
      println(s"Unsupported Operation for: $key")
      None
    operation
      .map:
        case m -> op => key -> m -> op
  end extractOperation

  private def createModel(key: String, method: Method, operation: Operation) =
    val name = generateServiceName(key, method)
    val topicName = generateServiceName(key, method, forTopic = true)
    val serviceIn = extractRequestBodyType(operation)
    val serviceOutPair = extractResponseBodyType(operation)

    BpmnServiceObject(
      name = name,
      topicName = topicName,
      path = key,
      descr = Option(operation.getDescription),
      method = method,
      respStatus = serviceOutPair.map(_._1),
      in = serviceIn,
      out = serviceOutPair.map(_._2),
      inputParams = createParams(operation.getParameters)
    )

  end createModel

  private def generateServiceName(key: String, method: Method, forTopic: Boolean = false) =
    val mStr = method.toString
    val methodStr = mStr.head + mStr.tail.toLowerCase
    val name = key
      .split("/")
      .filterNot:
        _.contains('{')
      .map: i =>
        i.split("-")
          .filter:
            _.trim.nonEmpty
          .map: e =>
            e.head.toUpper + e.tail
          .mkString
      .mkString
    if forTopic
    then name.head.toLower + name.tail + "." + methodStr.toLowerCase
    else name + methodStr
  end generateServiceName

  private def extractRequestBodyType(operation: Operation): Option[ConstrField] =
    Option(operation.getRequestBody)
      .flatMap: b =>
        for
          content <- Option(b.getContent)
          mediaType <- Option(content.get("application/json"))
          schema <- Option(mediaType.getSchema)
        yield schema.createField(
          optExample = Option(mediaType.getExample),
          optExamples = Option(mediaType.getExamples)
        )
  end extractRequestBodyType

  private def extractResponseBodyType(operation: Operation): Option[(String, ConstrField)] =
    def extract(resp: ApiResponse) =
      for
        content <- Option(resp.getContent)
        mediaType <- Option(content.get("application/json"))
        schema <- Option(mediaType.getSchema)
      yield schema.createField(
        optExample = Option(mediaType.getExample),
        optExamples = Option(mediaType.getExamples)
      )

    Option(operation.getResponses)
      .flatMap: b =>
        b.asScala
          .collectFirst:
            case k -> resp if k.startsWith("2") =>
              extract(resp)
                .map:
                  k -> _
          .flatten
  end extractResponseBodyType

  private def createParams(params: java.util.List[Parameter]): Option[Seq[ConstrField]] =
    Option(params)
      .map:
        _.asScala.toSeq
          .flatMap: param =>
            Option(param.getSchema)
              .map:
                _.createField(
                  Option(param.getName),
                  Option(param.getDescription),
                  Option(param.getRequired)
                )
              .toSeq

end BpmnClassesCreator
