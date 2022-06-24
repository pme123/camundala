package camundala
package api

import api.ast.*
import bpmn.*
import sttp.tapir.*
import sttp.tapir.docs.openapi.*
import sttp.tapir.openapi.*
import sttp.tapir.openapi.circe.yaml.*
import io.circe.*
import io.circe.syntax.*
import sttp.tapir.json.circe.*
import sttp.tapir.EndpointIO.Example

import java.text.SimpleDateFormat
import java.util.Date
import scala.util.matching.Regex

trait TapirApiCreator extends AbstractApiCreator:

  protected def create(apiDoc: ApiDoc): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    println(s"Start API: ${apiDoc.apis.size} top level APIs")
    apiDoc.apis.flatMap(_.create())

  extension (groupedApi: GroupedApi)
    def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      val apis = groupedApi.apis.flatMap(_.create(groupedApi.name))
      groupedApi match
        case pa: ProcessApi[?, ?] =>
          pa.createEndpoint(pa.name) ++ apis
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def create(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case aa @ ActivityApi(name, inOut, _) =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          aa.createEndpoint(tag)
        case pa @ ProcessApi(name, _, _, apis) if apis.isEmpty =>
          println(s"ProcessApi: $tag - $name")
          pa.createEndpoint(tag)
        case ga: GroupedApi =>
          throw IllegalArgumentException(
            "Sorry, only one level of GroupedApis are allowed!"
          )

  end extension

  extension (inOutApi: InOutApi[?, ?])

    def createEndpoint(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val endpointType = inOutApi.inOut.getClass.getSimpleName
      val tagPath = tag.replace(" ", "")
      val path =
        if (tagPath == inOutApi.id)
          endpointType.toLowerCase() / inOutApi.id
        else
          endpointType.toLowerCase() / tagPath / inOutApi.id
      Seq(
        endpoint
          .name(s"$endpointType: ${inOutApi.name}")
          .tag(tag)
          .in(path)
          .summary(s"$endpointType: ${inOutApi.name}")
          .description(inOutApi.descr)
          .head
      ).map(ep => inOutApi.toInput.map(ep.in).getOrElse(ep))
        .map(ep => inOutApi.toOutput.map(ep.out).getOrElse(ep))

    private def toInput: Option[EndpointInput[?]] =
      inOutApi.inOut.in match
        case _: NoInput =>
          None
        case _ =>
          Some(
            inOutApi.inMapper
              .examples(inOutApi.apiExamples.inputExamples.fetchExamples.map {
                case InOutExample(label, ex) =>
                  Example(
                    ex,
                    Some(label),
                    None
                  )
              }.toList)
          )

    private def toOutput: Option[EndpointOutput[?]] =
      inOutApi.inOut.out match
        case _: NoOutput =>
          None
        case _ =>
          Some(
            inOutApi.outMapper
              .examples(inOutApi.apiExamples.outputExamples.fetchExamples.map {
                case InOutExample(name, ex) =>
                  Example(
                    ex,
                    Some(name),
                    None
                  )
              }.toList)
          )
  end extension
end TapirApiCreator
