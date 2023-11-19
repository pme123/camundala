package camundala
package examples.twitter.api

import camundala.bpmn.*
import camundala.domain.*

object TwitterApi extends BpmnDsl:
  given tenantId: Option[String] = Some("{{tenantId}}")

  @description("""Every employee may create a Tweet.
                 |
                 |- email:   The email address of the creator.
                 |- content: The content of the Tweet.
                 |""".stripMargin)
  case class CreateTweet(
      //@description("Variables cannot be described as it is only possible to have one description per type!")
      email: String = "me@myself.com",
      content: String = "Test Tweet"
  )
  object CreateTweet:
    given Schema[CreateTweet] = Schema.derived
    given CirceCodec[CreateTweet] = deriveCodec
  end CreateTweet

  @description("""Every Tweet has to be accepted by the Boss.""")
  case class ReviewedTweet(
      @description("If true, the Boss accepted the Tweet")
      approved: Boolean = true
  )
  object ReviewedTweet:
    given Schema[ReviewedTweet] = Schema.derived
    given CirceCodec[ReviewedTweet] = deriveCodec
  end ReviewedTweet

  val twitterDemoProcess =
    val processId = "TwitterDemoP"
    process(
      id = processId,
      descr = "This runs the Twitter Approvement Process.",
      in = CreateTweet(),
      out = ReviewedTweet() // just for Testing
    )
  val reviewTweetApprovedUT = userTask(
    id = "ReviewTweetUT",
    in = NoInput(),
    out = ReviewedTweet()
  )
  val reviewTweetNotApprovedUT = userTask(
    id = "ReviewTweetUT",
    in = NoInput(),
    out = ReviewedTweet(false)
  )


