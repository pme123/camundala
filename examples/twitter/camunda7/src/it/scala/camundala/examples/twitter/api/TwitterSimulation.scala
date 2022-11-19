package camundala
package examples.twitter.api

import api.*
import bpmn.*
import simulation.*
import camundala.examples.twitter.api.TwitterApi.*
import camundala.simulation.gatling.GatlingSimulation
import io.circe.generic.auto.*
import io.gatling.http.request.builder.HttpRequestBuilder
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleTwitterC7/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends SimulationDsl, GatlingSimulation:

  override implicit def config =
    super.config.withPort(8887)

  private val `Twitter - Approved` = twitterDemoProcess
  private val `Twitter - Not Approved` = twitterDemoProcess
    .withOut(ReviewedTweet(false))

  private val `reviewTweet Not ApprovedUT` = reviewTweetApprovedUT
  .withOut(ReviewedTweet(false))

  simulate {
    scenario(`Twitter - Approved`)(
      reviewTweetApprovedUT
    )
    scenario(`Twitter - Not Approved`)(
      `reviewTweet Not ApprovedUT`
    )
  }
