package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TwitterApiCreator extends ApiCreator:

  val projectName = "twitter-example-c7"

  override val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "twitter" / "camunda7")
      .withPort(8887)

  def title = "Twitter Process API C7"

  def version = "1.0"

  document {
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  }

  private lazy val `Tweet accepted` = reviewTweetApprovedUT
  private lazy val ReviewTweetApprovedUT =
    `Tweet accepted`
      .withOutExample("Tweet rejected", ReviewedTweet(approved = false))


