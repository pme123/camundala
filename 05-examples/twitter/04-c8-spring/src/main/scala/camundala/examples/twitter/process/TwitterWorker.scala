package camundala.examples.twitter.process

import camundala.examples.twitter.api.ReviewedTweet
import camundala.examples.twitter.business.{DuplicateTweetException, TwitterService}
import io.camunda.zeebe.spring.client.annotation.{JobWorker, VariablesAsType}
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import scala.jdk.CollectionConverters.MapHasAsJava

@Component
class TwitterWorker:

  @Autowired
  private var twitterService: TwitterService = null

  @JobWorker(`type` = "publish-tweet", autoComplete = true)
  @throws[Exception]
  def handleTweet(@VariablesAsType variables: ReviewedTweet): Unit =
    try twitterService.tweet(variables.tweet)
    catch
      case ex: DuplicateTweetException =>
        throw new ZeebeBpmnError("duplicateMessage", "Could not post tweet, it is a duplicate.", Map.empty.asJava)

  @JobWorker(`type` = "send-rejection", autoComplete = true)
  @throws[Exception]
  def sendRejection(@VariablesAsType variables: ReviewedTweet): Unit =
    // same thing as above, do data transformation and delegate to real business code / service
    println(s"Sorry Tweet ${variables.tweet} rejected")
end TwitterWorker
