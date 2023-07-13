package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import TwitterApi.*


object TwitterApiCreator extends DefaultApiCreator:

  val projectName = "twitter-example-c7"

  val title = "Twitter Process API C7"

  val version = "1.0"

  document {
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  }

  override lazy val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "twitter" / "camunda7")
      .withPort(8887)

  private lazy val `Tweet accepted` = reviewTweetApprovedUT
  private lazy val ReviewTweetApprovedUT =
    `Tweet accepted`
      .withOutExample("Tweet rejected", ReviewedTweet(approved = false))


