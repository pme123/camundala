package pme123.camundala.examples.twitter.delegate

import java.io.File
import java.net.UnknownHostException

import com.danielasfregola.twitter4s.exceptions.TwitterException
import org.camunda.bpm.engine.delegate.{BpmnError, DelegateExecution}
import pme123.camundala.camunda.delegate.CamundaDelegate
import pme123.camundala.model.ModelLayers
import pme123.camundala.twitter.{twitterApi, twitterConfig}
import zio.Runtime.default.unsafeRun
import zio.ZIO

/**
  * Publish content on Twitter. It really goes live! Watch out http://twitter.com/#!/camunda_demo for your postings.
  */
class TweetContentDelegate
  extends CamundaDelegate {
  private val authFile = new File("./examples/twitter/resources/twitter-auth.conf")

  def execute(execution: DelegateExecution): Unit = {
    val logger = ModelLayers.logLayer("TweetContentDelegate")
    unsafeRun(
      (for {
        tweet <- execution.stringVar("content")
        _ <-
          if ("network error" == tweet)
            ZIO.fail(new UnknownHostException("demo twitter account"))
          else
            twitterApi.createTweet(tweet)
      } yield ())
        .mapError {
          case ex: TwitterException if ex.code.intValue == 187 =>
            new BpmnError("duplicateMessage")
        }.provideCustomLayer(logger ++ (logger >>> twitterConfig.live(authFile)) >>> twitterApi.live)
    )
  }

}
