package camundala
package dmn

import pme123.camunda.dmn.tester.shared.*
import sttp.client3.*
import sttp.client3.circe.*

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

trait DmnConfigWriter extends DmnTesterHelpers:

  def updateConfig(dmnConfig: DmnConfig, configPath: os.Path): Unit =
    val encodedPath =
      URLEncoder.encode(configPath.relativeTo(projectBasePath).toString, StandardCharsets.UTF_8)
    client.send(
      basicRequest
        .contentType("application/json")
        .body(dmnConfig)
        .put(uri"$apiUrl/dmnConfig?path=$encodedPath")
        .response(asString)
    ).body match
      case Right(_) =>
        println(s"Successfully updated ${dmnConfig.decisionId}")
      case Left(v)  => println(s"Problem updating $configPath: \n $v")
    end match
  end updateConfig

end DmnConfigWriter
