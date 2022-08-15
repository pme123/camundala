package camundala
package api

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
          pa.createEndpoint(pa.name, pa.additionalDescr) ++ apis
        case da: DecisionDmnApi[?, ?] =>
          da.createEndpoint(da.name)
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def create(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case aa @ DecisionDmnApi(name, inOut, _) =>
          aa.createEndpoint(tag)
        case aa @ ActivityApi(name, inOut, _) =>
          aa.createEndpoint(tag)
        case pa @ ProcessApi(name, _, _, apis) if apis.forall(_.isInstanceOf[ActivityApi[?,?]]) =>
          pa.createEndpoint(tag, pa.additionalDescr) ++ apis.flatMap(_.create(tag))
        case ga =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApi is allowed!\n - $ga"
          )

  end extension

  extension (inOutApi: InOutApi[?, ?])
    def createEndpoint(
        tag: String,
        additionalDescr: Option[String] = None
    ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val tagPath = tag.replace(" ", "")
      val path = inOutApi.inOut.in match
        case gs: GenericServiceIn =>
          inOutApi.endpointType.toLowerCase() / inOutApi.id / gs.serviceName
        case _ if tagPath == inOutApi.id =>
          if (inOutApi.name == inOutApi.id)
            inOutApi.endpointType.toLowerCase() / inOutApi.id
          else
            inOutApi.endpointType.toLowerCase() / inOutApi.id / inOutApi.name.replace(" ", "")
        case _ =>
          println(s"${inOutApi.name} == ${inOutApi.id}")
          if (inOutApi.name == inOutApi.id)
            inOutApi.endpointType.toLowerCase() / tagPath / inOutApi.id
          else
            inOutApi.endpointType.toLowerCase() / tagPath / inOutApi.id / inOutApi.name.replace(" ", "")

      Seq(
        endpoint
          .name(inOutApi.endpointName)
          .tag(tag)
          .in(path)
          .summary(inOutApi.endpointName)
          .description(inOutApi.descr + additionalDescr.getOrElse(""))
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

  extension (pa: ProcessApi[?, ?])
    def processName: String =
      pa.inOut.in match
        case gs: GenericServiceIn =>
          gs.serviceName
        case _ => pa.id

    def additionalDescr: Option[String] =
      val usedByDescr = UsedByReferenceCreator(processName).create()
      val usesDescr = UsesReferenceCreator(processName).create()
      Some(s"\n\n${usedByDescr.mkString}${usesDescr.mkString}")
  end extension

end TapirApiCreator
