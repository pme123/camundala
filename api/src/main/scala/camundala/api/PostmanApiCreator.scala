package camundala
package api

import ast.*
import bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.tapir.PublicEndpoint
import sttp.tapir.docs.openapi.*
import sttp.tapir.json.circe.*
import sttp.tapir.openapi.*
import sttp.tapir.*
import sttp.tapir.openapi.circe.yaml.*
import sttp.tapir.EndpointIO.Example

import java.text.SimpleDateFormat
import java.util.Date
import scala.reflect.ClassTag
import scala.util.matching.Regex

trait PostmanApiCreator extends AbstractApiCreator:

  protected def createPostman(
      apiDoc: ApiDoc
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    println(s"Start Postman API: ${apiDoc.apis.size} top level APIs")
    apiDoc.apis.flatMap(_.createPostman())

  extension (groupedApi: GroupedApi)
    def createPostman(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      val apis = groupedApi.apis.flatMap(_.createPostman(groupedApi.name))
      groupedApi match
        case pa: ProcessApi[?, ?] =>
          createPostmanForProcess(pa, pa.name) ++ apis
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def createPostman(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case pa @ ProcessApi(name, inOut, _, apis) if apis.isEmpty =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          createPostmanForProcess(pa, tag)
        case ga: GroupedApi =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApis are allowed!\n$ga"
          )
        case aa @ ActivityApi(name, inOut, _) =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          inOut match
            case _: UserTask[?, ?] =>
              createPostmanForUserTask(aa, tag)
            case other =>
              println(s"TODO: $other")
              Seq.empty

  end extension

  protected def createPostmanForProcess(
      api: ProcessApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    api.startProcess(tag)

  protected def createPostmanForUserTask(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    api.getActiveTask(tag)

  extension (api: InOutApi[?, ?])
    private def postmanBaseEndpoint(
        tag: String
    ): PublicEndpoint[?, Unit, Unit, Any] =
      endpoint
        .name(s"${api.typeName}: ${api.name}")
        .tag(tag)
        .summary(s"${api.typeName}: ${api.name}")
        .description(api.descr)

  end extension

  extension (process: ProcessApi[?, ?])

    def startProcess(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val path =
        val basePath = "process-definition" / "key" / process.inOut.id
        tenantId
          .map(id => basePath / "tenant-id" / tenantIdPath(id) / "start")
          .getOrElse(basePath / "start")

      Seq(
        process
          .postmanBaseEndpoint(tag)
          .in(path)
          .post
      ).map(ep =>
        process
          .toPostmanInput(
            jsonBody[StartProcessIn],
            (example: FormVariables) =>
              StartProcessIn(
                example,
                Some(process.name)
              )
          )
          .map(ep.in)
          .getOrElse(ep)
      )
  end extension

  extension (api: ActivityApi[?, ?])
    def getActiveTask(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println("TODO: getActiveTask") //TODO
      Seq.empty
  end extension

  private def tenantIdPath(id: String): EndpointInput[String] =
    path[String]("tenant-id")
      .description("The tenant, the process is deployed for.")
      .default(id)

  extension (inOutApi: InOutApi[?, ?])
    def toPostmanInput[
        T <: Product: Encoder: Decoder: Schema
    ](
        inMapper: EndpointIO.Body[String, T],
        wrapper: FormVariables => T
    ): Option[EndpointInput[T]] =
      inOutApi.inOut.in match
        case _: NoInput =>
          None
        case _ =>
          Some(
            inMapper
              .examples(
                inOutApi.apiExamples.inputExamples.fetchExamples.map {
                case ex @ InOutExample(label, _) =>
                  Example(
                    wrapper(ex.toCamunda),
                    Some(label),
                    None
                  )
              }.toList)
          )
  end extension
end PostmanApiCreator
