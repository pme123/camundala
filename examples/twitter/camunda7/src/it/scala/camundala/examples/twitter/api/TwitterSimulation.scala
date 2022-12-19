package camundala
package examples.twitter.api

import api.*
import bpmn.*
import simulation.*
import simulation.custom.CustomSimulation
import camundala.examples.twitter.api.TwitterApi.*
import io.circe.generic.auto.*
import sttp.tapir.generic.auto.*

import scala.concurrent.duration.*

// exampleTwitterC7/It/testOnly *TwitterSimulation
class TwitterSimulation extends CustomSimulation:

  override implicit def config =
    super.config.withPort(8034)

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
