package camundala
package simulation
package gatling

import api.CamundaProperty
import bpmn.*
import bpmn.CamundaVariable.{CFile, CFileValueInfo, CInteger, CJson}
import domain.*
import io.gatling.core.Predef.*
import io.gatling.core.check.CheckBuilder
import io.gatling.core.check.string.BodyStringCheckType
import io.gatling.core.structure.ChainBuilder
import io.gatling.http.Predef.*
import io.gatling.http.request.builder.HttpRequestBuilder

import scala.concurrent.duration.*
import io.circe.parser.*

trait SimulationHelper extends ResultChecker:

  implicit def config: SimulationConfig[HttpRequestBuilder] = SimulationConfig[HttpRequestBuilder]()

  extension (builder: HttpRequestBuilder)
    def auth(): HttpRequestBuilder =
      config.authHeader(builder)

  def loadVariable(variableName: String): ChainBuilder =
    exec(
      http(s"Load Variable '$variableName'")
        .get(
          s"/history/variable-instance?variableName=$variableName&processInstanceIdIn=#{processInstanceId}&deserializeValues=false"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJson("$[*].value", variableName)
        )
    ).exitHereIfFailed

  def checkMaxCount =
    val maxCount = config.maxCount
    bodyString
      .transformWithSession { (_: String, session: Session) =>
        if (session.attributes.contains("retryCount"))
          assert(
            session("retryCount").as[Int] <= maxCount,
            s"!!! The retryCount reached the maximum of $maxCount"
          )
      }

  def statusCondition(status: Int*): Session => Boolean = session => {
    println("<<< lastStatus: " + session("lastStatus").as[Int])
    println("<<< retryCount: " + session("retryCount").as[Int])
    val lastStatus = session("lastStatus").as[Int]
    !status.contains(lastStatus)
  }

  def taskCondition(): Session => Boolean = session => {
    println("<<< retryCount taskCondition: " + session("retryCount").as[Int])
    session.attributes.get("taskId").contains(null)
  }

  def processInstanceCondition(): Session => Boolean = session => {
    println(
      "<<< retryCount processInstanceCondition: " + session("retryCount")
        .as[Int]
    )
    session.attributes.get("processInstanceId").contains(null)
  }

  // check if the process is  not active
  def processFinishedCondition: Session => Boolean = session =>
    val status = session.attributes.get("processState")
    status.contains("ACTIVE")

  // check if there is a variable in the process with a certain value
  def processReadyCondition(key: String, value: Any): Session => Boolean =
    session =>
      val variable = session.attributes.get(key)
      println(
        s"<<< processReadyCondition: ${variable.getClass} - ${value.getClass} - ${!variable
          .contains(value.toString)}"
      )
      !variable.contains(value)

  // check if there is an incident in the session that contains the expected Message
  def incidentReadyCondition(errorMsg: String): Session => Boolean =
    session =>
      val variable = session.attributes.get("errorMsg")
      println(
        s"<<< incidentReadyCondition: ${variable
          .exists(_.asInstanceOf[String].contains(errorMsg))} - $errorMsg in: $variable"
      )
      variable != null && !variable.contains(null) && !variable.exists(
        _.asInstanceOf[String].contains(errorMsg)
      )

  def extractJson(path: String, key: String) =
    jsonPath(path)
      .ofType[Any]
      .transform { v =>
        println(s"<<< Extracted $key: $v"); v
      } // save the data
      .saveAs(key)

  def extractJsonOptional(path: String, key: String) =
    jsonPath(path)
      .ofType[Any]
      .transform { v =>
        println(s"<<< Extracted optional $key: $v"); v
      }
      .optional
      .saveAs(key)

  val printBody =
    bodyString.transform { b => println(s"<<< Response Body: $b") }

  val printSession: ChainBuilder =
    exec { session =>
      println(s"<<< Session: " + session)
      session
    }

  def retryOrFail(
      chainBuilder: ChainBuilder,
      condition: Session => Boolean = statusCondition(200)
  ) = {
    exec {
      _.set("lastStatus", -1)
        .set("retryCount", 0)
    }.doWhile(condition(_)) {
      exec()
        .pause(1.second)
        .exec(chainBuilder)
        .exec { session =>
          if (session("lastStatus").asOption[Int].nonEmpty)
            session.set("lastStatus", session("lastStatus").as[Int])
          else
            session
        }
        .exec(session =>
          session.set("retryCount", 1 + session("retryCount").as[Int])
        )
    }.exitHereIfFailed
  }

  protected def checkIncident(errorMsg: String): Seq[ChainBuilder] =
    Seq(
      exec(_.remove("errorMsg")),
      exec(_.remove("rootCauseIncidentId")),
      retryOrFail(
        getIncident(errorMsg),
        incidentReadyCondition(errorMsg)
      )
    )
  private def getIncident(errorMsg: String): ChainBuilder =
    exec(
      http(s"Check Incident '$errorMsg'")
        .get(
          s"/incident?processInstanceId=#{processInstanceId}"
        )
        .auth()
        .check(checkMaxCount)
        .check(
          extractJsonOptional("$[*].incidentMessage", "errorMsg")
        )
        .check(
          extractJsonOptional("$[*].rootCauseIncidentId", "rootCauseIncidentId")
        )
    ).doIf(session => session.attributes.get("errorMsg").contains(null)) {
      getRootIncident(errorMsg)
    }.exitHereIfFailed

  private def getRootIncident(errorMsg: String): ChainBuilder =
    doIf("#{rootCauseIncidentId.exists()}")(
      exec(session => {
        println(
          s"We are in the loop: ${session.attributes.get("rootCauseIncidentId")}"
        )
        session
      })
        .exec(
          http(s"Check Root Incident '$errorMsg'")
            .get(
              s"/incident?rootCauseIncidentId=#{rootCauseIncidentId}"
            )
            .auth()
            .check(checkMaxCount)
            .check(
              extractJsonOptional("$[*].incidentMessage", "errorMsg")
            )
        )
        .exitHereIfFailed
    )
