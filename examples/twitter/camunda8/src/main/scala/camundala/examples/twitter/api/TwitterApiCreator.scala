package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.twitter.api.TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

object TwitterApiCreator extends ApiCreator :

  override protected val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples"  / "twitter" / "camunda8")
      .withPort(8887)

  def title = "Twitter Process API"

  def version = "1.0"

  private val ReviewTweetApprovedUT =
    reviewTweetApprovedUT
      .withOutExample("Tweet accepted", ReviewedTweet())
      .withOutExample("Tweet rejected", ReviewedTweet(approved = false))

  document {
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  }
