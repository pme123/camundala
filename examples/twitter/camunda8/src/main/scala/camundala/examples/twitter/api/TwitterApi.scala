package camundala.examples.twitter.api

import camundala.bpmn.*

object TwitterApi extends BpmnDsl:
  implicit def tenantId: Option[String] = Some("{{tenantId}}")

  @description("Every employee may create a Tweet.")
  case class Tweet(
      tweet: String = "Hello Tweet",
      author: String = "pme123",
      boss: String = "Great Master"
  )

  @description("Every Tweet has to be accepted by the Boss.")
  case class ReviewedTweet(
      tweet: String = "Hello Tweet",
      author: String = "pme123",
      boss: String = "Great Master",
      approved: Boolean = false
  )

  val twitterDemoProcess =
    val processId = "TwitterDemoProcess"
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

  val TweetHandledEEIdent = "end_event_tweet_published"
  lazy val TweetHandledEE = endEvent(
    TweetHandledEEIdent,
    descr = "Process ended - Tweet was published."
  )
  val TweetRejectedEEIdent = "end_event_tweet_rejected"
  lazy val TweetRejectedEE = endEvent(
    TweetRejectedEEIdent,
    descr = "Process ended - Tweet was rejected."
  )

  given Schema[Tweet] = Schema.derived
  given Encoder[Tweet] = deriveEncoder
  given Decoder[Tweet] = deriveDecoder
  given Schema[ReviewedTweet] = Schema.derived
  given Encoder[ReviewedTweet] = deriveEncoder
  given Decoder[ReviewedTweet] = deriveDecoder