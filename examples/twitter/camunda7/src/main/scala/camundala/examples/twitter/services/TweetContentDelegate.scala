package camundala.examples.twitter.services

import twitter4j.*

import java.net.UnknownHostException

/**
 * Publish content on Twitter.
 */
class TweetContentDelegate extends JavaDelegate :

  @throws[Exception]
  override def execute(execution: DelegateExecution): Unit =
    val content = execution.getVariable("content").asInstanceOf[String]
    // Force a network error
    if ("network error" == content) throw new UnknownHostException("demo twitter account")
    val tokenSecret = "YOUR TOKEN SECRET"
    val consumerSecret = "YOUR CONSUMER SECRET"

    val twitter = Twitter.newBuilder
      .oAuthConsumer("consumer key", consumerSecret)
      .oAuthAccessToken("token key", tokenSecret)
      .build
    twitter.v1.tweets.updateStatus(content)

