package org.camunda.community.examples.twitter.rest

import io.camunda.zeebe.client.ZeebeClient
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent
import org.camunda.community.examples.twitter.process.{ApprovedTweet, Tweet}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, MediaType, ResponseEntity}
import org.springframework.web.bind.annotation.{PutMapping, RestController}
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.bind.annotation.RequestBody

@RestController
class ReviewTweetRestApi:

  @Autowired
  private var zeebeClient: ZeebeClient = _

  @PutMapping(
    value = Array("/tweet"),
    consumes = Array(MediaType.APPLICATION_JSON_VALUE)
  )
  def startTweetReviewProcess(
      @RequestBody
      tweet: Tweet
  ): ResponseEntity[String] = {
    val reference = startTweetReview(tweet)
    // And just return something for the sake of the example
    ResponseEntity
      .status(HttpStatus.OK)
      .body("Started process instance " + reference)
  }

  def startTweetReview(
      processVariables: Tweet
  ): String = {
    val processInstance = zeebeClient.newCreateInstanceCommand
      .bpmnProcessId("TwitterDemoProcess")
      .latestVersion
      .variables(processVariables)
      .send
      .join // blocking call!
    String.valueOf(processInstance.getProcessInstanceKey)
  }
