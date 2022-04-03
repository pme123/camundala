package camundala
package examples.twitter.api

import api.*
import bpmn.*
import domain.*
import gatling.*
import camundala.examples.twitter.api.TwitterApi.*
import io.circe.generic.auto.*
import io.gatling.http.request.builder.HttpRequestBuilder
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleTwitterC8/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends BasicSimulationRunner:

  override implicit def config: SimulationConfig =
    super.config.withPort(8887)

  simulate(
    processScenario("Twitter - Approved")(
      twitterDemoProcess,
      reviewTweetApprovedUT,
    //  TweetHandledEE
    ),
    processScenario("Twitter - Not Approved")(
      twitterDemoProcess
        .withOut(ReviewedTweet(approved = false)),
      reviewTweetApprovedUT
        .withOut(ReviewedTweet(approved = false)),
   //   TweetRejectedEE
    )
  )
