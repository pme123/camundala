package camundala
package api

import camundala.bpmn.DecisionDmn
import io.circe.{Decoder, Encoder}
import sttp.model.StatusCode
import sttp.tapir.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import sttp.tapir.generic.auto.*

import scala.reflect.ClassTag

case class EvaluateDecision[
    In <: Product: Encoder: Decoder: Schema: ClassTag,
    Out <: Product: Encoder: Decoder: Schema: ClassTag
](
    decisionDmn: DecisionDmn[In, Out],
    restApi: CamundaRestApi[In, Out]
) extends ApiEndpoint[In, Out, EvaluateDecision[In, Out]]:

  val outStatusCode = StatusCode.Ok
  val endpointType = "DecisionDmn"
  val apiName = decisionDmn.decisionDefinitionKey

  def withRestApi(
      restApi: CamundaRestApi[In, Out]
  ): EvaluateDecision[In, Out] =
    copy(restApi = restApi)

  override lazy val descr: String = restApi.maybeDescr.getOrElse("") +
    s"""
       |
       |Decision DMN:
       |- _decisionDefinitionKey_: `$apiName`,
       |""".stripMargin

  def createPostman()(implicit
      tenantId: Option[String]
  ): Seq[PublicEndpoint[?, ?, ?, ?]] =
    Seq(
      postmanBaseEndpoint
        .in(postPath(apiName))
        .post
    )

  private def postPath(name: String)(implicit tenantId: Option[String]) =
    val basePath =
      "decision-definition" / "key" / definitionKeyPath(name)
    tenantId
      .map(id => basePath / "tenant-id" / tenantIdPath(id) / "evaluate")
      .getOrElse(basePath / "evaluate") / s"--REMOVE:${restApi.name}--"

  override protected def inMapperPostman() =
    restApi.inMapper[EvaluateDecisionIn] { (example: In) =>
      EvaluateDecisionIn(
        CamundaVariable.toCamunda(example)
      )
    }
    
end EvaluateDecision

