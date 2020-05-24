package pme123.camundala.examples.playground

import eu.timepit.refined.auto._
import org.springframework.boot.autoconfigure.SpringBootApplication
import pme123.camundala.app.appRunner.AppRunner
import pme123.camundala.examples.common.StandardCliApp
import pme123.camundala.model.bpmn.StaticFile
import pme123.camundala.services.StandardApp
import pme123.camundala.services.StandardApp.StandardAppDeps
import zio.ZLayer

@SpringBootApplication(scanBasePackages = Array("pme123.camundala.examples.playground", "pme123.camundala.camunda"))
class PlaygroundApp

object PlaygroundApp extends StandardCliApp {

  protected val ident: String = "playground"
  protected val title = "Camundala Playground App"

  protected def appRunnerLayer: ZLayer[StandardAppDeps, Nothing, AppRunner] =
    StandardApp.layer(classOf[PlaygroundApp], StaticFile("playgroundModels.sc", "."))

}
