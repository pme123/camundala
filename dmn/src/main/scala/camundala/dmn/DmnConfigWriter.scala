package camundala
package dmn

import domain.*
import pme123.camunda.dmn.tester.shared.*
import io.circe.generic.auto.*
import sttp.client3.*
import sttp.client3.circe.*
import io.circe.generic.auto.*
import os.Path

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.annotation.tailrec

trait DmnConfigWriter extends DmnTesterStarter:

  def updateConfig(dmnConfig: DmnConfig, configPath: Path): Unit =
    val encodedPath = URLEncoder.encode(configPath.relativeTo(projectBasePath).toString, StandardCharsets.UTF_8)
    println(s"updateConfig: $encodedPath")
    waitForServer
    client.send(
      basicRequest
        .contentType("application/json")
        .body(dmnConfig)
        .put(uri"$apiUrl/dmnConfig?path=$encodedPath")
        .response(asString)
    ).body match
      case Right(_) =>
        println(s"Successfully updated $configPath" )
        println("Check it on http://localhost:8883")
      case Left(v) => println(s"Problem updating $configPath: \n $v" )

  @tailrec
  private def waitForServer: Boolean =
      if(checkIsRunning())
        true
      else
        println("Waiting for server")
        Thread.sleep(1000)
        waitForServer

end DmnConfigWriter

