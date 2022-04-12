package camundala.examples.twitter.process

import io.camunda.zeebe.spring.client.annotation.{ZeebeVariablesAsType, ZeebeWorker}
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError
import camundala.examples.twitter.api.TwitterApi.ReviewedTweet
import camundala.examples.twitter.business.{DuplicateTweetException, TwitterService}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class TwitterWorker :

  @Autowired
  private var twitterService: TwitterService = null

  @ZeebeWorker(`type` = "publish-tweet", autoComplete = true)
  @throws[Exception]
  def handleTweet(@ZeebeVariablesAsType variables: ReviewedTweet): Unit = {
    try twitterService.tweet(variables.tweet)
    catch {
      case ex: DuplicateTweetException =>
        throw new ZeebeBpmnError("duplicateMessage", "Could not post tweet, it is a duplicate.")
    }
  }

  @ZeebeWorker(`type` = "send-rejection", autoComplete = true)
  @throws[Exception]
  def sendRejection(@ZeebeVariablesAsType variables: ReviewedTweet): Unit = {
    // same thing as above, do data transformation and delegate to real business code / service
    println(s"Sorry Tweet ${variables.tweet} rejected")
  }

