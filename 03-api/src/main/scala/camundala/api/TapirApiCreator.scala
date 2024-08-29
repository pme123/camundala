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
      case cApi: CApi => throw IllegalArgumentException(
          s"Sorry, the top level must be a GroupedApi (Group or Process)!\n - Not ${cApi.getClass}"
        )
    }
  end create

  extension (groupedApi: GroupedApi)
    def create(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${groupedApi.name}")
      groupedApi match
        case pa: ProcessApi[?, ?, ?] =>
          pa.createEndpoint(pa.id, false, pa.additionalDescr) ++
            pa.createInitEndpoint(pa.id) ++
            pa.apis.flatMap(_.create(pa.id, false))
        case _: CApiGroup =>
          groupedApi.apis.flatMap(_.create(groupedApi.name, true))
      end match
    end create

  end extension

  extension (cApi: CApi)
    def create(tag: String, tagIsFix: Boolean): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case da @ DecisionDmnApi(_, _, _, _) =>
          da.createEndpoint(tag, tagIsFix, da.additionalDescr)
        case aa @ ActivityApi(_, _, _) =>
          aa.createEndpoint(tag, tagIsFix)
        case pa @ ProcessApi(name, _, _, apis, _)
            if apis.isEmpty =>
          pa.createEndpoint(tag, tagIsFix, pa.additionalDescr)
        case spa: ExternalTaskApi[?, ?] =>
          spa.createEndpoint(tag, tagIsFix, spa.additionalDescr)
        case ga =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApi is allowed!\n - $ga"
          )

  end extension

  extension (processApi: ProcessApi[?, ?, ?])
    // creates the Init Worker Endpoint - each Process has one (not for GenericService)
    def createInitEndpoint(tagFull: String): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      if hasInitIn then
        val eTag = shortenTag(tagFull)
        Seq(
          endpoint
            .name("Init Worker")
            .tag(eTag)
            .in(processApi.path(eTag) / "init")
            .summary("Init Worker")
            .description(
              s"""|${processApi.inOut.initInDescr.mkString}
                  |
                  |The Init Worker has the following responsibilities:
                  |
                  |  - Validates the Process Input (`In`). -> by Camundala
                  |  - Maps the Configuration to Process Variables (`InConfig`). -> by Camundala
                  |  - Custom validation the Process Input (`In`, e.g. combining 2 variables). -> Process Specific
                  |  - Initializes the default Variables with `In` as input (see _Process_ description) and `InitIn` as output. -> Process Specific
                  |
                  |`NoInput` means there is no initialization needed.
                  |""".stripMargin
            )
            .head
        ) // renders `In` as input: .map(ep => processApi.toInput.map(ep.in).getOrElse(ep))
          .map(ep => ep.out(processApi.toInitIn))
      else
        Seq.empty
    end createInitEndpoint

    private def hasInitIn: Boolean =
      processApi.inOut.initIn match
        case _: NoOutput =>
          false
        case i if i.getClass == processApi.inOut.in.getClass =>
          false
        case _ =>
          true
    end hasInitIn

    private def toInitIn: EndpointOutput[?] =
      processApi.initInMapper
        .examples(
          List(
            Example(
              processApi.inOut.initIn,
              Some("InitIn"),
              None
            )
          )
        )
  end extension
  extension (inOutApi: InOutApi[?, ?])

    def createEndpoint(
        tagFull: String,
        tagIsFix: Boolean,
        additionalDescr: Option[String] = None
    ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      val eTag = if tagIsFix then tagFull else shortenTag(tagFull)
      println(s"createEndpoint: $tagIsFix $tagFull >> $eTag")
      val endpointName = if inOutApi.name == tagFull then "Process" else inOutApi.endpointName
      Seq(
        endpoint
          .name(endpointName)
          .tag(eTag)
          .in(path(eTag))
          .summary(endpointName)
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

    def path(tag: String): EndpointInput[Unit] =
      val refId = refIdentShort(inOutApi.id, projectName)
      val tagPath = tag.replace(" ", "")

      inOutApi.inOut.in match
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
      end match
    end path

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
