package org.camunda.community.examples.twitter.process

case class ApprovedTweet(
    tweet: String = "Hello Tweet",
    author: String = "pme123",
    boss: String = "Great Master",
    approved: Boolean = false
)
