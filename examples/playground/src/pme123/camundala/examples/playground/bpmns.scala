package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import pme123.camundala.camunda.service.restService.Request
import pme123.camundala.camunda.service.restService.Request.Host
import pme123.camundala.camunda.service.restService.RequestPath.Path
import pme123.camundala.model.deploy.Url

object bpmns {

  val swapiUrl: Url = "https://swapi.dev/api"

  val swapiHost: Host = Host(swapiUrl)

  def swapiRequest(path: Path, responseVar: String = "jsonResult"): Request =
    Request(
      swapiHost,
      path = path,
      responseVariable = responseVar
    )
}
