package camundala.examples.twitter
package api

import TwitterApi.*
import camundala.examples.twitter.services
import camundala.examples.twitter.services.*
import camundala.bpmn.*

import camundala.test.*
import org.junit.Test
import org.mockito.Mockito.mock

trait TwitterUnitTests extends CommonTesting:
  lazy val config: TestConfig =
    testConfig
      .deployments(
        baseResource / "example-twitter.bpmn",
        formResource / "createTweet.html",
        formResource / "reviewTweet.html"
      )
      .registries(
        serviceRegistry(
          services.emailAdapter,
          mock(classOf[RejectionNotificationDelegate])
        ),
        serviceRegistry(
          services.tweetAdapter,
          mock(classOf[TweetContentOfflineDelegate])
        )
      )

  @Test
  def testApprovedPath(): Unit =
    test(twitterDemoProcess)(
      reviewTweetApprovedUT,
      TweetHandledEE
    )

  @Test
  def testRejectedPath(): Unit =
    test(
      twitterDemoProcess
        .withOut(ReviewedTweet(false))
    )(
      reviewTweetNotApprovedUT,
      TweetHandledEE
    )

class ExampleTwitterTest extends TestRunner, TwitterUnitTests

class ExampleTwitterScenario extends ScenarioRunner, TwitterUnitTests
