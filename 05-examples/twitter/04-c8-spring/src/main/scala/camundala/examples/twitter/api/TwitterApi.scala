package camundala.examples.twitter.api

import camundala.domain.*

object TwitterApi extends BpmnProcessDsl:
  val processName = "TwitterDemoP"
  val descr = "This runs the Twitter Approvement Process."
  val path: String = "DELETE /services/method"

  val twitterDemoProcess =
    process(
      in = Tweet(),
      out = ReviewedTweet() // just for Testing
    )

  val reviewTweetApprovedUT = userTask(
    id = "user_task_review_tweet",
    in = Tweet(),
    out = ReviewedTweet()
  )
  val reviewTweetNotApprovedUT = userTask(
    id = "user_task_review_tweet",
    in = Tweet(),
    out = ReviewedTweet(approved = false)
  )
end TwitterApi

@description("Every employee may create a Tweet.")
case class Tweet(
    tweet: String = "Hello Tweet",
    author: String = "pme123",
    boss: String = "Great Master"
)

object Tweet:
  given ApiSchema[Tweet] = deriveApiSchema
  given InOutCodec[Tweet] = deriveCodec

@description("Every Tweet has to be accepted by the Boss.")
case class ReviewedTweet(
    tweet: String = "Hello Tweet",
    author: String = "pme123",
    boss: String = "Great Master",
    approved: Boolean = false
)

object ReviewedTweet:
  given ApiSchema[ReviewedTweet] = deriveApiSchema
  given InOutCodec[ReviewedTweet] = deriveCodec

@description("Every Tweet has to be accepted by the Boss.")
case class TweetOut(
    tweet: String = "Hello Tweet",
    author: String = "pme123",
    boss: String = "Great Master",
    endStatus: EndStatus = EndStatus.published
)

enum EndStatus:
  case published, notPublished
object EndStatus:
  given ApiSchema[EndStatus] = deriveApiSchema
  given InOutCodec[EndStatus] = deriveEnumInOutCodec

object TweetOut:
  given ApiSchema[TweetOut] = deriveApiSchema
  given InOutCodec[TweetOut] = deriveCodec
