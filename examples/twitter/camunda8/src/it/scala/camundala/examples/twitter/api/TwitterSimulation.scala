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

// exampleTwitterC8/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends SimulationDsl:

  override implicit def config: SimulationConfig =
    super.config.withPort(8887)

  private val `Twitter - Not Approved` =
    twitterDemoProcess
      .withOut(ReviewedTweet(approved = false))
  private val reviewTweetNotApprovedUT =
    reviewTweetApprovedUT
      .withOut(ReviewedTweet(approved = false))

  simulate {
    scenario(twitterDemoProcess)(
      reviewTweetApprovedUT
    )
    scenario(`Twitter - Not Approved`)(
      reviewTweetNotApprovedUT
    )
  }
