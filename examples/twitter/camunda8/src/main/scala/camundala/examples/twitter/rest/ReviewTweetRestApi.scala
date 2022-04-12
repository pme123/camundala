package camundala.examples.twitter
package rest

import api.TwitterApi.Tweet
import camundala.examples.twitter.camunda.{RestEndpoint, Validator}
import org.springframework.web.bind.annotation.{PutMapping, RequestBody, RestController}

@RestController
class ReviewTweetRestApi extends RestEndpoint :

  @PutMapping(value = Array("/tweet"))
  def startTweetReviewProcess(
      @RequestBody
      tweet: Tweet
  ): Response =
    startProcess("TwitterDemoProcess", tweet)
