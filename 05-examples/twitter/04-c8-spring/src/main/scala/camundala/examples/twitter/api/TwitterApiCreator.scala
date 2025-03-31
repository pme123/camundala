package camundala.examples.twitter.api

import camundala.api.*
import camundala.examples.twitter.api.TwitterApi.*

// exampleTwitterC8/run
object TwitterApiCreator extends DefaultApiCreator:

  val projectName = "twitter-example-c8"

  override val apiConfig: ApiConfig =
    ApiConfig("demoCompany")
      .withBasePath(os.pwd / "05-examples" / "twitter" / "camunda8")
      .withDocBaseUrl("https://webstor.ch/camundala/myCompany")
      .withPort(8887)

  def title = "Twitter Process API C8"

  def version                            = "1.0"
  lazy val companyProjectVersion: String = "0.1.0"

  document {
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  }

  private lazy val ReviewTweetApprovedUT =
    reviewTweetApprovedUT
      .withOutExample("Tweet accepted", ReviewedTweet(approved = true))
      .withOutExample("Tweet rejected", ReviewedTweet())

  lazy val companyDescr: String = ""
  lazy val projectDescr: String = ""
end TwitterApiCreator
