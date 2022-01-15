package camundala
package examples.invoice.bpmn

import api.*
import bpmn.*
import domain.*
import gatling.*
import camundala.examples.twitter.bpmn.TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleTwitter/GatlingIt/testOnly *TwitterSimulation
class TwitterSimulation extends BasicSimulationRunner:

  override val serverPort = 8887

  simulate(
    processScenario("Twitter - Approved")(
      twitterDemoProcess,
      reviewTweetApprovedUT.getAndComplete()
    ),
    processScenario("Twitter - Not Approved")(
      twitterDemoProcess
        .withOut(ReviewTweet(false)),
      reviewTweetApprovedUT
        .withOut(ReviewTweet(false)).getAndComplete()
    )
  )
