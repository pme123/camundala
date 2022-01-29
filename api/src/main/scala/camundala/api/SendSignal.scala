package camundala.api

import camundala.bpmn.*
import camundala.domain.*
import io.circe.syntax.*
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.*

import scala.reflect.ClassTag

case class SendSignal[
    In <: Product: Encoder: Decoder: Schema: ClassTag
](
    event: ReceiveMessageEvent[In],
    restApi: CamundaRestApi[In, NoOutput]
) extends ApiEndpoint[In, SendSignalIn, NoOutput, SendSignal[In]]:

  val outStatusCode = StatusCode.NoContent
  val endpointType = "Signal"
  val apiName = event.messageName

  def withRestApi(
      restApi: CamundaRestApi[In, NoOutput]
  ): SendSignal[In] =
    copy(restApi = restApi)

  override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
    s"""
       |
       |Signal:
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
      SendSignalIn(
        apiName,
        tenantId = tenantId,
        variables = Some(CamundaVariable.toCamunda(example))
      )
    }

end SendSignal
