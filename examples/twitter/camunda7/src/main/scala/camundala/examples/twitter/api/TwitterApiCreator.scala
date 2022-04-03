package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TwitterApiCreator extends APICreator:

  def title = "Twitter Process API"

  def version = "1.0"

  override lazy val serverPort = 8887

  override def basePath: Path = pwd / "examples" / "twitter" / "camunda7"

  def docProjectUrl(project: String): String =
    s"https://MYDOCHOST/$project"

  apiEndpoints(
    twitterDemoProcess
      .endpoints(
        reviewTweetApprovedUT.endpoint
          .withOutExample("Tweet accepted", ReviewTweet())
          .withOutExample("Tweet rejected", ReviewTweet(false))
      )
  )
