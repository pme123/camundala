package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.twitter.api.TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TwitterApiCreator extends APICreator :

  lazy val projectName = "TwitterDemo"

  def title = "Twitter Process API"

  def version = "1.0"

  override lazy val serverPort = 8887

  override def basePath: Path = pwd / "examples" / "twitter" / "camunda8"

  def docProjectUrl(project: String): String =
    s"https://MYDOCHOST/$project"

  apiEndpoints(
    twitterDemoProcess
      .endpoints(
        reviewTweetApprovedUT.endpoint
          .withOutExample("Tweet accepted", ReviewedTweet())
          .withOutExample("Tweet rejected", ReviewedTweet(approved = false)),
      )
  )
