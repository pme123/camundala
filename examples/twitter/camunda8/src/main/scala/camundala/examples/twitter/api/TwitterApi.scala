package camundala.examples.twitter.api

import camundala.bpmn.*
import camundala.domain.*

object TwitterApi extends BpmnDsl:
  implicit def tenantId: Option[String] = Some("{{tenantId}}")

  val twitterDemoProcess =
    val processId = "TwitterDemoP"
    process(
      id = processId,
      descr = "This runs the Twitter Approvement Process.",
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

@description("Every employee may create a Tweet.")
case class Tweet(
                  tweet: String = "Hello Tweet",
                  author: String = "pme123",
                  boss: String = "Great Master"
                )

object Tweet:
  given Schema[Tweet] = Schema.derived
  given Encoder[Tweet] = deriveEncoder
  given Decoder[Tweet] = deriveDecoder

@description("Every Tweet has to be accepted by the Boss.")
case class ReviewedTweet(
                          tweet: String = "Hello Tweet",
                          author: String = "pme123",
                          boss: String = "Great Master",
                          approved: Boolean = false
                        )

object ReviewedTweet:
  given Schema[ReviewedTweet] = Schema.derived
  given Encoder[ReviewedTweet] = deriveEncoder
  given Decoder[ReviewedTweet] = deriveDecoder

@description("Every Tweet has to be accepted by the Boss.")
case class TweetOut(
                          tweet: String = "Hello Tweet",
                          author: String = "pme123",
                          boss: String = "Great Master",
                          endStatus: EndStatus = EndStatus.published
                        )

enum EndStatus derives ConfiguredEnumCodec :
  case published, notPublished

object TweetOut:
  given Schema[EndStatus] = Schema.derived
  given Schema[TweetOut] = Schema.derived
  given Encoder[TweetOut] = deriveEncoder
  given Decoder[TweetOut] = deriveDecoder
