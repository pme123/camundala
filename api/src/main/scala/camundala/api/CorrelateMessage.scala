package camundala.api

import camundala.bpmn.*

import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.*

import scala.reflect.ClassTag

case class CorrelateMessage[
    In <: Product: Encoder: Decoder: Schema: ClassTag
](
    event: ReceiveMessageEvent[In],
    restApi: CamundaRestApi[In, NoOutput]
) extends ApiEndpoint[In, CorrelateMessageIn, NoOutput, CorrelateMessage[In]]:

  val outStatusCode = StatusCode.NoContent
  val endpointType = "Message"
  val apiName = event.messageName

  def withRestApi(
      restApi: CamundaRestApi[In, NoOutput]
  ): CorrelateMessage[In] =
    copy(restApi = restApi)

  override lazy val descr: String = restApi.maybeDescr.getOrElse("NO DESCR") +
    s"""
       |
       |$endpointType:
       |- _messageName_: `$apiName`,
       |""".stripMargin

  def createPostman()(using
                           tenantId: Option[String]
  ) =
    Seq(
      postmanBaseEndpoint
        .in(postPath(apiName))
        .post
    )

  private def postPath(name: String) = "signal"

  override protected def inMapperPostman()(using
      tenantId: Option[String]
  ) =
    restApi.inMapper { (example: In) =>
      CorrelateMessageIn(
        apiName,
        tenantId = tenantId,
        processVariables = Some(CamundaVariable.toCamunda(example))
      )
    }

end CorrelateMessage
