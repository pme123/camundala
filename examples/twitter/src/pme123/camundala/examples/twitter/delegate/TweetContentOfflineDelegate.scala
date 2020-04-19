package pme123.camundala.examples.twitter.delegate

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.springframework.stereotype.Service
import pme123.camundala.twitter.twitterApi
import zio.Runtime.default.unsafeRun
import zio.console.Console

/**
  * Use this delegate instead of TweetContentDelegate, if you don't want to access Twitter, but just to do some sysout.
  */
@Service("tweetAdapter")
class TweetContentOfflineDelegate
  extends CamundaDelegate {

  def execute(execution: DelegateExecution): Unit =
    unsafeRun(
      (for {
        tweet <- execution.stringVar("content")
        _ <- twitterApi.createTweet(tweet)
      } yield ())
        .provideCustomLayer(Console.live >>> twitterApi.offline)
    )
}
