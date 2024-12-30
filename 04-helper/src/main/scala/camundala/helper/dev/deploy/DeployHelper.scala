package camundala.helper.dev.deploy

import camundala.helper.util.{PostmanConfig, Helpers}
import os.proc

import java.util.Date

case class DeployHelper(postmanConfig: PostmanConfig) extends Helpers:

  val collectionId = postmanConfig.collectionId
  val envId = postmanConfig.localDevEnvId
  val postmanApiKey = sys.env(postmanConfig.envApiKey)

  def deploy(integrationTest: Option[String] = None): Unit =
    println(s"Publishing Project locally")
    val time = new Date().getTime

    os.proc("sbt", "publishLocal").callOnConsole()

    os.proc(
      "newman",
      "run",
      s"https://api.getpostman.com/collections/$collectionId?apikey=$postmanApiKey",
      "-e",
      s"https://api.getpostman.com/environments/$envId?apikey=$postmanApiKey",
      "--folder",
      "deploy_manifest",
      "--global-var",
      s"developer=${System.getProperty("user.name").toUpperCase}"
    ).callOnConsole()

    integrationTest.map { test =>
      val testName = if test == "all" then "" else test
      os.proc("sbt", "-J-Xmx3G", s"simulation/testOnly *$testName").callOnConsole()
    }

    println(s"Deploy and test finished in ${(new Date().getTime - time) / 1000} s")
  end deploy
end DeployHelper
