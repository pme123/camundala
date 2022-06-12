package camundala
package examples.twitter.api

import api.*
import bpmn.*
import simulation.*

import camundala.examples.twitter.api.TwitterApi.*
import io.circe.generic.auto.*
import io.gatling.http.request.builder.HttpRequestBuilder
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleTwitterC7/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends SimulationDsl:

  override implicit def config: SimulationConfig =
    super.config.withPort(8887)

  private val `Twitter - Approved` = twitterDemoProcess
  private val `Twitter - Not Approved` = twitterDemoProcess
    .withOut(ReviewTweet(false))

  private val `reviewTweet Not ApprovedUT` = reviewTweetApprovedUT
  .withOut(ReviewTweet(false))

  simulate {
    scenario(`Twitter - Approved`)(
      reviewTweetApprovedUT
    )
    scenario(`Twitter - Not Approved`)(
      `reviewTweet Not ApprovedUT`
    )
  }
