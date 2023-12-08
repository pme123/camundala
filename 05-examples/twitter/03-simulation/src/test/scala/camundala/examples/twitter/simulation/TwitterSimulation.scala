package camundala.examples.twitter
package simulation

import bpmn.TwitterApi.*
import camundala.bpmn.*
import camundala.simulation.*
import camundala.simulation.custom.CustomSimulation

// exampleTwitterSimulation/test
// exampleTwitterSimulation/testOnly *TwitterSimulation
class TwitterSimulation extends CustomSimulation:

  simulate (
    scenario(`Twitter - Approved`)(
      reviewTweetApprovedUT
    ),
    scenario(`Twitter - Not Approved`)(
      `reviewTweet Not ApprovedUT`
    )
  )

  override implicit def config =
    super.config.withPort(8034)

  private lazy val `Twitter - Approved` = twitterDemoProcess
  private lazy val `Twitter - Not Approved` = twitterDemoProcess
    .withOut(ReviewedTweet(false))

  private lazy val `reviewTweet Not ApprovedUT` = reviewTweetApprovedUT
  .withOut(ReviewedTweet(false))
