package camundala.examples.twitter.api

import camundala.api.*
import camundala.bpmn.*
import camundala.examples.twitter.api.TwitterApi.*

// exampleTwitterC8/run
object TwitterApiCreator extends DefaultApiCreator:

  val projectName = "twitter-example-c8"

  override val apiConfig: ApiConfig =
    super.apiConfig
      .withBasePath(pwd / "examples" / "twitter" / "camunda8")
      .withDocProjectUrl(project => s"https://webstor.ch/camundala/myCompany/$project")
      .withPort(8887)

  def title = "Twitter Process API C8"

  def version = "1.0"

  document {
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  }

  private lazy val ReviewTweetApprovedUT =
    reviewTweetApprovedUT
      .withOutExample("Tweet accepted", ReviewedTweet())
      .withOutExample("Tweet rejected", ReviewedTweet(approved = false))
