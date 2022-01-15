package camundala
package examples.twitter
package bpmn

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.twitter.bpmn.TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TwitterApiCreator extends APICreator:

  def title = "Twitter Process API"

  def version = "1.0"

  override lazy val serverPort = 8887

  override def basePath: Path = pwd / "examples" / "twitter"

  apiEndpoints(
    twitterDemoProcess
      .endpoints(
        reviewTweetApprovedUT.endpoint
          .withOutExample("Tweet accepted", ReviewTweet())
          .withOutExample("Tweet rejected", ReviewTweet(false))
      )
  )
