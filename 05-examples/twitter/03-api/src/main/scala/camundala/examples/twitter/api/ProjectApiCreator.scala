package camundala.examples.twitter.api

import camundala.domain.*
import camundala.api.*
import camundala.examples.twitter.bpmn.*
import camundala.examples.twitter.bpmn.TwitterApi.*

// exampleTwitterApi/run
object ProjectApiCreator extends DefaultApiCreator:

  val title = "Twitter Process API C7"

  val version = "1.0"

  lazy val companyProjectVersion: String = "0.1.0"
  lazy val projectDescr: String          = ""

  document(
    api(twitterDemoProcess)(
      ReviewTweetApprovedUT
    )
  )

  override lazy val apiConfig: ApiConfig =
    ApiConfig("demoCompany")
      .withBasePath(os.pwd / "05-examples" / "twitter")
      .withPort(8887)

  private lazy val `Tweet accepted`      = reviewTweetApprovedUT
  private lazy val ReviewTweetApprovedUT =
    `Tweet accepted`
      .withOutExample("Tweet rejected", ReviewedTweet(approved = false))
end ProjectApiCreator
