package camundala
package api

import bpmn.*
import io.circe.*
import io.circe.syntax.*
import sttp.tapir.PublicEndpoint
import sttp.apispec.openapi.*
import sttp.tapir.*
import sttp.apispec.openapi.circe.yaml.*
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

  extension (cApi: CApi)
    def createPostman(): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      println(s"Start Grouped API: ${cApi.name}")
      cApi match
        case pa: ProcessApi[?, ?] =>
          createPostmanForProcess(pa, pa.name) ++ pa.apis.flatMap(
            _.createPostman(cApi.name)
          )
        case da: DecisionDmnApi[?, ?] =>
          createPostmanForDecisionDmn(da.toActivityApi, da.name)
        case gApi: CApiGroup =>
          gApi.apis.flatMap(_.createPostman(cApi.name, true))
        case _: CApi =>
          cApi.createPostman(cApi.name, true)

  end extension

  extension (cApi: CApi)
    def createPostman(
        tag: String,
        isGroup: Boolean = false
    ): Seq[PublicEndpoint[?, Unit, ?, Any]] =
      cApi match
        case pa @ ProcessApi(name, inOut, _, apis, _) if apis.isEmpty =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          createPostmanForProcess(pa, tag, isGroup)
        case aa @ ActivityApi(name, inOut, _) =>
          println(s"${inOut.getClass.getSimpleName}: $tag - $name")
          inOut match
            case _: UserTask[?, ?] =>
              createPostmanForUserTask(aa, tag)
            case _: DecisionDmn[?, ?] =>
              createPostmanForDecisionDmn(aa, tag)
            case _: ReceiveMessageEvent[?] =>
              createPostmanForReceiveMessageEvent(aa, tag)
            case _: ReceiveSignalEvent[?] =>
              createPostmanForReceiveSignalEvent(aa, tag)
            case other =>
              println(s"TODO: $other")
              Seq.empty
        case pa @ ProcessApi(name, _, _, apis, _)
            if apis.forall(_.isInstanceOf[ActivityApi[?, ?]]) =>
          createPostmanForProcess(pa, tag) ++ apis.flatMap(_.createPostman(tag))
        case da: DecisionDmnApi[?,?] =>
          createPostmanForDecisionDmn(da.toActivityApi, tag)
        case ga =>
          throw IllegalArgumentException(
            s"Sorry, only one level of GroupedApi is allowed!\n - $ga"
          )

  end extension

  protected def createPostmanForProcess(
      api: ProcessApi[?, ?],
      tag: String,
      isGroup: Boolean = false
  ): Seq[PublicEndpoint[?, Unit, ?, Any]]

  protected def createPostmanForUserTask(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]]

  protected def createPostmanForDecisionDmn(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]]

  protected def createPostmanForReceiveMessageEvent(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]]

  protected def createPostmanForReceiveSignalEvent(
      api: ActivityApi[?, ?],
      tag: String
  ): Seq[PublicEndpoint[?, Unit, ?, Any]]

  extension (api: InOutApi[?, ?])
    protected def postmanBaseEndpoint(
        tag: String,
        input: Option[EndpointInput[?]],
        label: String,
        descr: Option[String] = None
    ): PublicEndpoint[?, Unit, Unit, Any] =
      val anchor = s"#operation/${api.endpointName.replace(" ", "%20")}"
      Some(
        endpoint
          .tag(tag)
          .summary(s"${api.name}: $label")
          .description(
            s"""${descr.getOrElse(api.descr)}
               |
               |See API Doc: [${api.name}](${apiConfig
              .docProjectUrl(projectName)}/OpenApi.html$anchor)
               |""".stripMargin
          )
        // .securityIn(auth.bearer()(sttp.tapir.Codec.cookies)) could not be imported to Postman:(

      ).map(ep =>
        input
          .map(ep.in)
          .getOrElse(ep)
      ).get

  end extension

  extension (inOutApi: InOutApi[?, ?])
    protected def endpointPath(isGroup: Boolean): EndpointInput[Unit] =
      inOutApi.inOut.in match
        case gs: GenericServiceIn =>
          inOutApi.id / s"--REMOVE${gs.serviceName}--"
        case _ =>
          if (isGroup)
            inOutApi.id / s"--REMOVE${inOutApi.name.replace(" ", "")}--"
          else
            inOutApi.id

  protected def tenantIdPath(
      basePath: EndpointInput[?],
      pathElem: String
  ): EndpointInput[?] =
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / pathElem)
      .getOrElse(basePath / pathElem)

  protected def tenantIdPath(id: String): EndpointInput[String] =
    path[String]("tenant-id")
      .description("The tenant, the process is deployed for.")
      .default(id)

  protected def taskIdPath() =
    path[String]("taskId")
      .description("""The taskId of the Form.
                     |> This is the result id of the `GetActiveTask`
                     |
                     |Add in the _Tests_ panel of _Postman_:
                     |```
                     |let result = pm.response.json()[0];
                     |pm.collectionVariables.set("taskId", result.id)
                     |```
                     |""".stripMargin)
      .default("{{taskId}}")

  protected def definitionKeyPath(key: String): EndpointInput[String] =
    path[String]("key")
      .description(
        "The Process- or Decision-DefinitionKey of the Process or Decision"
      )
      .default(key)

end PostmanApiCreator
