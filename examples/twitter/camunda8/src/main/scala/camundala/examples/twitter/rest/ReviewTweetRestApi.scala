package camundala
package examples.twitter
package rest

import camundala.bpmn.*
import camundala.domain.*
import camundala.camunda8.*
import camundala.examples.twitter.api.*
import io.circe
import io.circe.parser.*
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation.*

@RestController
class ReviewTweetRestApi extends RestEndpoint :

  @PostMapping(value = Array("/process-definition/TwitterDemoProcess/create"))
  def createTweetReviewProcess(
                               @RequestBody
                               json: String
                             ): Response =
    createInstance("TwitterDemoProcess", validate[CreateProcessInstanceIn[Tweet, NoOutput]](json))

  @PostMapping(value = Array("/process-definition/TwitterDemoProcessAuto/create"))
  def createTweetReviewProcessAuto(
                               @RequestBody
                               json: String
                             ): Response =
    createInstance("TwitterDemoProcessAuto", validate[CreateProcessInstanceIn[Tweet, NoOutput]](json))
