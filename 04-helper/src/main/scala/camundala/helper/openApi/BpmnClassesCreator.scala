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
      .flatMap:
        case key -> path =>
          extractOperations(key, path)
      .map:
        case key -> method -> operation =>
          createModel(key, method, operation)
      .toSeq
  end create

  private def extractOperations(key: String, path: PathItem): Seq[((String, Method), Operation)] =
    Map(
      Method.GET    -> path.getGet,
      Method.POST   -> path.getPost,
      Method.PUT    -> path.getPut,
      Method.PATCH  -> path.getPatch,
      Method.DELETE -> path.getDelete
    ).toSeq.collect:
      case method -> (op: Operation) =>
        key -> method -> op
  end extractOperations

  private def createModel(key: String, method: Method, operation: Operation) =
    val name           = generateServiceName(key, method)
    val topicName      = generateServiceName(key, method, forTopic = true)
    val serviceIn      = extractRequestBodyType(operation)
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
    val mStr      = method.toString
    val methodStr = mStr.head + mStr.tail.toLowerCase
    val name      = key
      .split("/")
      .filterNot(_.isBlank)
      .filterNot: x =>
        val f = (config.filterNames.contains(x))
        f
      .map:
        case i if i.startsWith("{") =>
          i.tail.init // {id} => id
        case i => i
      .map: i =>
        i.split("-")
          .filter:
            _.trim.nonEmpty
          .map: e =>
            e.head.toUpper + e.tail
          .mkString
      .distinct
      .mkString
    methodStr + name
  end generateServiceName

  private def extractRequestBodyType(operation: Operation): Option[ConstrField] =
    Option(operation.getRequestBody)
      .flatMap: b =>
        for
          content   <- Option(b.getContent)
          mediaType <- Option(content.get("application/json"))
          schema    <- Option(mediaType.getSchema)
        yield schema.createField(
          optExample = Option(mediaType.getExample),
          optExamples = Option(mediaType.getExamples)
        )
  end extractRequestBodyType

  private def extractResponseBodyType(operation: Operation): Option[(String, ConstrField)] =
    def extract(resp: ApiResponse) =
      for
        content   <- Option(resp.getContent)
        mediaType <- Option(content.get("application/json"))
        schema    <- Option(mediaType.getSchema)
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
