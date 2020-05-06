import pme123.camundala.examples.twitter.bpmn.deployments

object Main extends App {
  val result = deployments.deploys
  println(s"Deploys: $result")
}