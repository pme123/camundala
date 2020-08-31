package pme123.camundala.examples.twitter

import eu.timepit.refined.auto._
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.services.appRunner.AppRunner
import pme123.camundala.examples.common.StandardExampleCliApp
import pme123.camundala.model.bpmn.StaticFile
import pme123.camundala.services.StandardApp
import pme123.camundala.services.StandardApp.StandardAppDeps
import zio._

@SpringBootApplication(scanBasePackages = Array("pme123.camundala.camunda"))
class TwitterApp

object TwitterApp extends StandardExampleCliApp {

  protected val ident: String = "twitter"
  protected val title = "Twitter Camundala Demo App"

  protected def appRunnerLayer: ZLayer[StandardAppDeps, Nothing, AppRunner] =
    StandardApp.layer(classOf[TwitterApp], StaticFile("bpmnModels.sc", "bpmn"))

}
