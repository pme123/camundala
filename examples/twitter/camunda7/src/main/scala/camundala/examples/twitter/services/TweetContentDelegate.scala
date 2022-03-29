package camundala.examples.twitter.services

import twitter4j.auth.AccessToken
import twitter4j.{Twitter, TwitterFactory}

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
    val token = "YOUR TOKEN"
    val tokenSecret = "YOUR TOKEN SECRET"
    val accessToken = new AccessToken(token, tokenSecret)
    val twitter = new TwitterFactory().getInstance
    val consumerKey = "YOUR CONSUMER KEY"
    val consumerSecret = "YOUR CONSUMER SECRET"
    twitter.setOAuthConsumer(consumerKey, consumerSecret)
    twitter.setOAuthAccessToken(accessToken)
    twitter.updateStatus(content)
