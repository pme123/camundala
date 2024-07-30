package camundala
package api

import camundala.bpmn.*
import camundala.domain.*
import sttp.tapir.EndpointIO.Example

trait TapirApiCreator extends AbstractApiCreator:

  protected def create(apiDoc: ApiDoc): Seq[PublicEndpoint[?, Unit, ?, Any]] =
    println(s"Start API: ${apiDoc.apis.size} top level APIs")
    apiDoc.apis.flatMap {
      case groupedApi: GroupedApi => groupedApi.create()
      case cApi: CApi => cApi.create("")
    }
  end create

  extension (groupedApi: GroupedApi)
    def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      val apis = groupedApi.apis.flatMap(_.create(groupedApi.name))
      groupedApi match
        case pa: ProcessApi[?, ?, ?] =>
          pa.createEndpoint(pa.name, pa.additionalDescr) ++ apis
        case _: CApiGroup => apis

  end extension

  extension (cApi: CApi)
    def create(tag: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case da @ DecisionDmnApi(_, _, _, _) =>
          da.createEndpoint(tag, da.additionalDescr)
        case aa @ ActivityApi(_, _, _) =>
          aa.createEndpoint(tag)
        case pa @ ProcessApi(name, _, _, apis, _)
            if apis.isEmpty =>
          pa.createEndpoint(tag, pa.additionalDescr)
        case spa: ExternalTaskApi[?, ?] =>
          spa.createEndpoint(tag, spa.additionalDescr)
        case ga =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApi is allowed!\n - $ga"
          )

  end extension

  extension (inOutApi: InOutApi[?, ?])
    def createEndpoint(
        tagFull: String,
        additionalDescr: Option[String] = None
    ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val tagOrig = refIdentShort(tagFull)
      val tag =
        if tagOrig == tagFull then
          tagOrig
        else
          tagOrig.head.toUpper + tagOrig.tail.map {
            case c: Char if c.isUpper => s" $c"
            case c => s"$c"
          }.mkString
      val refId = refIdentShort(inOutApi.id, projectName)
      val tagPath = tag.replace(" ", "")
      val path: EndpointInput[Unit] = inOutApi.inOut.in match
        case gs: GenericServiceIn =>
          inOutApi.inOutType.toString / refId / gs.serviceName
        case _ if tagPath == refId =>
          if inOutApi.name == refId then
            inOutApi.inOutType.toString / refId
          else
            inOutApi.inOutType.toString / refId / inOutApi.name
              .replace(" ", "")
        case _ =>
          if inOutApi.name == refId then
            inOutApi.inOutType.toString / tagPath / refId
          else
            inOutApi.inOutType.toString / tagPath / refId / inOutApi.name.replace(
              " ",
              ""
            )

      Seq(
        endpoint
          .name(inOutApi.endpointName)
          .tag(tag)
          .in(path)
          .summary(inOutApi.endpointName)
          .description(
            inOutApi.apiDescription(
              apiConfig.diagramDownloadPath,
              apiConfig.diagramNameAdjuster
            ) + additionalDescr.getOrElse("")
          )
          .head
      ).map(ep => inOutApi.toInput.map(ep.in).getOrElse(ep))
        .map(ep => inOutApi.toOutput.map(ep.out).getOrElse(ep))
    end createEndpoint

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

  extension (pa: ProcessApi[?, ?, ?] | ExternalTaskApi[?, ?])
    def processName: String =
      pa.inOut.in match
        case gs: GenericServiceIn =>
          gs.serviceName
        case _ => pa.id

    def additionalDescr: Option[String] =
      if apiConfig.projectsConfig.isConfigured then
        val usedByDescr = UsedByReferenceCreator(processName).create()
        val usesDescr = UsesReferenceCreator(processName).create()
        Some(s"\n\n${usedByDescr.mkString}${usesDescr.mkString}")
      else None
  end extension

  extension (dmn: DecisionDmnApi[?, ?])
    def additionalDescr: Option[String] =
      if apiConfig.projectsConfig.isConfigured then
        val usedByDescr = UsedByReferenceCreator(dmn.id).create()
        Some(s"\n\n${usedByDescr.mkString}")
      else None
  end extension

end TapirApiCreator
